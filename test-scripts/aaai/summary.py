#  Genetic Conflict Seeker
#
#  Copyright (c) 2025
#
#  @author: Viet-Man Le (vietman.le@ist.tugraz.at)

#  Genetic Conflict Seeker
#
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

from core.conf import read_config, SummaryConfig
from core.utils import get_summary_files, process_all_summaries, add_identified_cs_per_time_column, \
    add_consistency_checks_per_identified_cs, runtime_to_string, add_improvement_column, calculate_average_stats, \
    calculate_std_stats

# This is a workaround to set the root project folder as the current working directory
ROOT_PROJECT_FOLDER = Path(__file__).resolve().parent.parent.parent
os.chdir(ROOT_PROJECT_FOLDER)
sys.path.insert(0, os.getcwd())

def main():
    """Main entry point"""
    all_config = read_config(prog="summary.py",
                             description="Summary Evaluation Results",
                             root=ROOT_PROJECT_FOLDER,
                             config_class=SummaryConfig)

    kbs = all_config.kbs
    eval_confs = all_config.eval_confs
    export_names = all_config.export_names

    for kb_name, n_cs in kbs.items():
        print(f"Processing Knowledge Base: {kb_name} with {n_cs} CS")

        # SUMMARY of summary files
        results_dir = ROOT_PROJECT_FOLDER / all_config.working_dir / kb_name / all_config.result_dirname
        summary_files = get_summary_files(results_dir, eval_confs)

        for summary_file in summary_files:
            exists = str(os.path.exists(summary_file))
            print("{:<70} {:<10}".format(summary_file, exists))
        print('')

        summary_table = process_all_summaries(summary_files, n_cs)
        summary_table = runtime_to_string(summary_table)
        summary_table = add_consistency_checks_per_identified_cs(summary_table)
        summary_table = add_identified_cs_per_time_column(summary_table)

        summary_table = add_improvement_column(summary_table,'cc_improv[%]', 'checks_per_cs', 'original', less_is_better=True)
        summary_table = add_improvement_column(summary_table,'cs_improv[%]', 'found_cs', 'original')
        summary_table = add_improvement_column(summary_table,'actual_improv[%]', 'cs_per_min', 'original')

        # rename config names
        summary_table['config_name'] = summary_table.index.map(lambda x: export_names[x])

        # change [%] to _pct and [ms] to _ms
        summary_table = summary_table.rename(columns={
            'cc_improv[%]': 'cc_improv_pct',
            'cs_improv[%]': 'cs_improv_pct',
            'runtime[m:s:ms]': 'runtime',
            'actual_improv[%]': 'actual_improv_pct'
        })

        # print all rows of the summary table
        pd.set_option('display.max_rows', None)
        pd.set_option('display.max_columns', None)
        pd.set_option('display.width', None)
        pd.set_option('display.max_colwidth', None)
        pd.set_option('display.float_format', '{:.2f}'.format)
        print(summary_table.to_string(index=False))

        # save the summary table to a csv file
        summary_dir = ROOT_PROJECT_FOLDER / all_config.working_dir / all_config.summary_dirname
        summary_dir.mkdir(parents=True, exist_ok=True)
        filename = summary_dir / f"{kb_name}_summary.csv"
        summary_table.to_csv(filename, index=False)

        # SUMMARY of stats files
        for conf in eval_confs:
            directory = ROOT_PROJECT_FOLDER / all_config.working_dir / kb_name / all_config.result_dirname / conf
            print(f"Directory: {directory}")

            mean_df = calculate_average_stats(directory)
            print(f"Mean DataFrame: {mean_df}")

            # save the mean DataFrames to CSV files
            filename = summary_dir / f"{kb_name}_{conf}_average_stats.csv"
            mean_df.to_csv(filename, index=False)

            # calculate stadard deviation
            std_df = calculate_std_stats(directory)
            print(f"Standard Deviation DataFrame: {std_df}")

            # save the standard deviation DataFrames to CSV files
            filename = summary_dir / f"{kb_name}_{conf}_std_stats.csv"
            std_df.to_csv(filename, index=False)


if __name__ == "__main__":
    main()