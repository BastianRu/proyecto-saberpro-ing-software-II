#!/usr/bin/env bash
# Builds the Taller 12 report (docx + pdf) from informe.md. Requires pandoc, python3 and LibreOffice.
set -euo pipefail
cd "$(dirname "$0")"
NAME=Taller12-InformeAutenticacionKeycloak
python3 make_reference.py reference.docx
python3 prepare_md.py informe.md > .informe-build.md
pandoc .informe-build.md -f markdown-auto_identifiers -o "$NAME.docx" --reference-doc=reference.docx --resource-path=.
soffice --headless --convert-to pdf "$NAME.docx" >/dev/null 2>&1
rm -f .informe-build.md
echo "Built: $NAME.docx and $NAME.pdf"
