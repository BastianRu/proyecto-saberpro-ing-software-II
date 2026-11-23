#!/usr/bin/env python3
"""Replaces references to missing figures by an explicit 'capture pending' note (never fakes evidence)."""
import re, sys, os
base = os.path.dirname(os.path.abspath(sys.argv[1]))
text = open(sys.argv[1], encoding="utf-8").read()
def fix(m):
    caption, path = m.group(1), m.group(2)
    if caption and not os.path.exists(os.path.join(base, path)):
        return f"**[CAPTURA PENDIENTE: {caption}]**"
    return m.group(0)
print(re.sub(r'!\[([^\]]*)\]\(([^)]+)\)(\{[^}]*\})?', fix, text))
