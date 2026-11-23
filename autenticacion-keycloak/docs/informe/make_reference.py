#!/usr/bin/env python3
"""Creates reference.docx (styles used by pandoc) from pandoc's default, adding the 'Portada' style,
Arial fonts, table borders and centered figure captions."""
import re, subprocess, sys, zipfile, shutil, tempfile, os

out = sys.argv[1]
default = subprocess.run(["pandoc", "-o", "/dev/stdout", "--print-default-data-file", "reference.docx"],
                         capture_output=True, check=True).stdout
tmp = tempfile.mkdtemp()
src = os.path.join(tmp, "default.docx")
open(src, "wb").write(default)

with zipfile.ZipFile(src) as zin, zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as zout:
    for item in zin.infolist():
        data = zin.read(item.filename)
        if item.filename == "word/styles.xml":
            xml = data.decode("utf-8")
            # Arial everywhere (replace theme fonts in docDefaults and styles)
            xml = re.sub(r'<w:rFonts [^>]*/>', '<w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial" w:eastAsia="Arial"/>', xml)
            # 11pt body text
            xml = re.sub(r'(<w:docDefaults>.*?<w:rPr>)', r'\1<w:sz w:val="22"/>', xml, count=1, flags=re.S)
            # Portada style (centered, spaced)
            portada = ('<w:style w:type="paragraph" w:customStyle="1" w:styleId="Portada"><w:name w:val="Portada"/>'
                       '<w:basedOn w:val="Normal"/><w:qFormat/><w:pPr><w:spacing w:before="240" w:after="240"/>'
                       '<w:jc w:val="center"/></w:pPr><w:rPr><w:sz w:val="24"/></w:rPr></w:style>')
            xml = xml.replace("</w:styles>", portada + "</w:styles>")
            # centered, italic figure captions
            xml = re.sub(r'(<w:style w:type="paragraph" w:styleId="ImageCaption">.*?<w:pPr>)', r'\1<w:jc w:val="center"/>', xml, count=1, flags=re.S)
            # table borders
            xml = re.sub(r'(<w:style w:type="table" w:customStyle="1" w:styleId="Table">.*?<w:tblPr>)',
                         r'\1<w:tblBorders><w:top w:val="single" w:sz="4" w:color="808080"/><w:left w:val="single" w:sz="4" w:color="808080"/>'
                         r'<w:bottom w:val="single" w:sz="4" w:color="808080"/><w:right w:val="single" w:sz="4" w:color="808080"/>'
                         r'<w:insideH w:val="single" w:sz="4" w:color="808080"/><w:insideV w:val="single" w:sz="4" w:color="808080"/></w:tblBorders>',
                         xml, count=1, flags=re.S)
            data = xml.encode("utf-8")
        zout.writestr(item, data)
shutil.rmtree(tmp)
