"""
Convert a JaCoCo XML coverage report to Cobertura XML format.

JaCoCo is the coverage tool used by jacoco-maven-plugin.
Cobertura XML is the format expected by CI systems for coverage visualisation.

Usage:
    python3 scripts/jacoco_to_cobertura.py target/site/jacoco/jacoco.xml \
        test_reports/coverage/coverage.xml
"""

import sys
import time
import xml.etree.ElementTree as ET


def _int(element: ET.Element, attr: str) -> int:
    return int(element.get(attr, "0"))


def _rate(covered: int, total: int) -> str:
    if total == 0:
        return "1.0"
    return str(round(covered / total, 4))


def convert(jacoco_path: str, cobertura_path: str) -> None:
    """Convert jacoco_path (JaCoCo XML) and write Cobertura XML to cobertura_path."""
    root = ET.parse(jacoco_path).getroot()

    total_lines = total_covered_lines = 0
    total_branches = total_covered_branches = 0

    packages_elem = ET.Element("packages")

    for package in root.findall("package"):
        pkg_name = package.get("name", "").replace("/", ".")
        pkg_lines = pkg_covered = pkg_branches = pkg_covered_branches = 0

        pkg_elem = ET.SubElement(
            packages_elem,
            "package",
            name=pkg_name,
            line_rate="0",
            branch_rate="0",
            complexity="0",
        )
        classes_elem = ET.SubElement(pkg_elem, "classes")

        for sourcefile in package.findall("sourcefile"):
            sf_name = sourcefile.get("name", "")
            pkg_path = package.get("name", "")
            filename = pkg_path + "/" + sf_name if pkg_path else sf_name
            class_name = sf_name.removesuffix(".java")

            sf_lines = sf_covered = sf_branches = sf_covered_branches = 0

            class_elem = ET.SubElement(
                classes_elem,
                "class",
                name=class_name,
                filename=filename,
                line_rate="0",
                branch_rate="0",
                complexity="0",
            )
            ET.SubElement(class_elem, "methods")
            lines_elem = ET.SubElement(class_elem, "lines")

            for line in sourcefile.findall("line"):
                nr = line.get("nr", "0")
                ci = _int(line, "ci")
                mi = _int(line, "mi")
                cb = _int(line, "cb")
                mb = _int(line, "mb")

                sf_lines += 1
                if ci > 0:
                    sf_covered += 1

                has_branch = (cb + mb) > 0
                if has_branch:
                    sf_branches += cb + mb
                    sf_covered_branches += cb

                line_attrs = {
                    "number": nr,
                    "hits": str(ci),
                    "branch": str(has_branch).lower(),
                }
                if has_branch and (cb + mb) > 0:
                    pct = int(100 * cb / (cb + mb))
                    line_attrs["condition-coverage"] = (
                        str(pct) + "% (" + str(cb) + "/" + str(cb + mb) + ")"
                    )

                ET.SubElement(lines_elem, "line", **line_attrs)

            class_elem.set("line_rate", _rate(sf_covered, sf_lines))
            class_elem.set("branch_rate", _rate(sf_covered_branches, sf_branches))

            pkg_lines += sf_lines
            pkg_covered += sf_covered
            pkg_branches += sf_branches
            pkg_covered_branches += sf_covered_branches

        pkg_elem.set("line_rate", _rate(pkg_covered, pkg_lines))
        pkg_elem.set("branch_rate", _rate(pkg_covered_branches, pkg_branches))

        total_lines += pkg_lines
        total_covered_lines += pkg_covered
        total_branches += pkg_branches
        total_covered_branches += pkg_covered_branches

    coverage = ET.Element(
        "coverage",
        attrib={
            "line-rate": _rate(total_covered_lines, total_lines),
            "branch-rate": _rate(total_covered_branches, total_branches),
            "lines-covered": str(total_covered_lines),
            "lines-valid": str(total_lines),
            "branches-covered": str(total_covered_branches),
            "branches-valid": str(total_branches),
            "complexity": "0",
            "version": "1.9",
            "timestamp": str(int(time.time())),
        },
    )
    sources = ET.SubElement(coverage, "sources")
    ET.SubElement(sources, "source").text = "src/main/java"
    coverage.append(packages_elem)

    ET.indent(coverage, space="  ")
    with open(cobertura_path, "w", encoding="utf-8") as out:
        out.write('<?xml version="1.0" encoding="UTF-8"?>\n')
        ET.ElementTree(coverage).write(out, encoding="unicode", xml_declaration=False)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print(
            "Usage: " + sys.argv[0] + " <jacoco.xml> <output_cobertura.xml>",
            file=sys.stderr,
        )
        sys.exit(1)
    convert(sys.argv[1], sys.argv[2])
