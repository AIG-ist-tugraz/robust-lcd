#!/usr/bin/env python3
"""
csv2latex.py

Read a CSV file and produce a LaTeX table using booktabs.

❯ python csv2latex.py summary/arcade_summary.csv arcade_summary.tex \
    --caption "Arcade summary" \
    --label "tab:arcade_summary"
"""

#  Genetic Conflict Seeker
#
#  Copyright (c) 2025
#
#  @author: Viet-Man Le (vietman.le@ist.tugraz.at)

#  Genetic Conflict Seeker
#
#
#  @author: Viet-Man Le (vietman.le@ist.tugraz.at)

import os
import sys
from pathlib import Path

import pandas as pd

from core.conf import read_config, CSV2LatexConfig

# This is a workaround to set the root project folder as the current working directory
ROOT_PROJECT_FOLDER = Path(__file__).resolve().parent.parent.parent
os.chdir(ROOT_PROJECT_FOLDER)
sys.path.insert(0, os.getcwd())

def csv_to_latex(input_csv: str,
                 output_tex: str,
                 caption: str = "",
                 label: str = "",
                 index: bool = False,
                 included_columns: list = None,
                 headers: dict = None):
    """
    Convert a CSV file to a LaTeX table.

    Parameters
    ----------
    input_csv : str
        Path to input CSV.
    output_tex : str
        Path to write LaTeX code.
    caption : str
        Table caption (optional).
    label : str
        Table label for referencing (optional).
    index : bool
        Whether to include the DataFrame index as a column.
    included_columns : list
        List of columns to include in the output table.
    headers : dict
        Dictionary mapping original column names to new headers.
    """
    # Read CSV
    try:
        df = pd.read_csv(ROOT_PROJECT_FOLDER / input_csv)
        if included_columns:
            df = df[included_columns] # reorder columns
    except Exception as e:
        sys.exit(f"Error reading '{input_csv}': {e}")

    # Rename columns if headers are provided
    if headers:
        df.rename(columns=headers, inplace=True)

    # Produce LaTeX
    latex_str = df.to_latex(
        index=index,
        escape=True,         # escape underscores, etc.
        caption=caption,
        label=label,
        longtable=False,     # set True if your table is very long
        float_format="%.2f"
    )

    # Write out
    try:
        with open(output_tex, 'w') as f:
            f.write(latex_str)
    except Exception as e:
        sys.exit(f"Error writing '{output_tex}': {e}")

def main():
    config = read_config(
        prog="csv2latex",
        description="Convert a CSV file to a LaTeX table (booktabs).",
        root=ROOT_PROJECT_FOLDER,
        config_class=CSV2LatexConfig
    )

    csv_to_latex(
        config.input_csv,
        config.output_tex,
        caption=config.caption,
        label=config.label,
        included_columns=config.included_columns,
        headers=config.headers
    )
    print(f"✓ LaTeX table written to {config.output_tex}")

if __name__ == "__main__":
    main()