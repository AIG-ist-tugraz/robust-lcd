#  Genetic Conflict Seeker
#
#  Copyright (c) 2026
#
#  @author: Viet-Man Le (vietman.le@ist.tugraz.at)

#  Genetic Conflict Seeker
#
#
#  @author: Viet-Man Le (vietman.le@ist.tugraz.at)

import numpy as np
import os
import pandas as pd
import sys
from _collections import defaultdict
from matplotlib import pyplot as plt
from pathlib import Path
from typing import List, Dict

from core.conf import read_config, VizConfig

# This is a workaround to set the root project folder as the current working directory
ROOT_PROJECT_FOLDER = Path(__file__).resolve().parent.parent.parent
os.chdir(ROOT_PROJECT_FOLDER)
sys.path.insert(0, os.getcwd())


def main():
    """Main entry point"""
    all_config = read_config(prog="viz.py",
                             description="Visualize Evaluation Results",
                             root=ROOT_PROJECT_FOLDER,
                             config_class=VizConfig)

    kbs = all_config.kbs

    summary_dir = ROOT_PROJECT_FOLDER / all_config.working_dir / all_config.summary_dirname

    viz_stats_files(all_config, summary_dir)

    viz_summary_files(kbs, summary_dir)


def plot(title: str, categories: List, values: Dict,
         max_cs: Dict,
         std_devs: Dict,
         ylabel: str,
         path_to_save: str):
    width = 1  # the width of the bars
    x = np.arange(len(categories)) * (width * len(values.items()) + 4)  # the label locations
    multiplier = 0

    fig, ax = plt.subplots(layout='constrained')  #

    for attribute, measurement in values.items():
        offset = width * multiplier
        errors = std_devs.get(attribute) if std_devs else None
        rects = ax.bar(x + offset, measurement, width, label=attribute,
                       yerr=errors,
                       capsize=1,
                       error_kw={'elinewidth': 1, 'ecolor': 'red', 'capsize': 1.5}  # Style for error bars
        )
        multiplier += 1

    # Add a horizontal line for the average
    if max_cs:
        # ylim = np.max(list(max_cs.values()))
        ylim = np.max([max(v) for v in values.values()])

        # ax.set_ylim(0, np.max([ylim + 10, 0.0]))
        ax.set_ylim(0, np.max([ylim + 170, 0.0]))

        for i, (model, value) in enumerate(max_cs.items()):
            # if model == 'ea':
            #     continue

            xmin = x[i] - 0.55
            xmax = x[i] + (width * (len(values.items())))

            # draw a horizontal line for the average
            showed_value = value
            if value > ylim:
                value = ylim + 110

            ax.plot([xmin, xmax], [value, value], color='blue', linestyle='dashed', linewidth=1)
            ax.text(xmax, value, f"{showed_value}", color='blue', fontsize=7, ha='left', va='bottom')

    # Add some text for labels, title and custom x-axis tick labels, etc.
    # ax.set_title(title)
    ax.set_xticks(x + width * (len(values) - 1) / 2)
    ax.set_xticklabels(categories, fontsize=10, fontname='Times')
    ax.set_ylabel(ylabel=ylabel, fontsize=10, fontname='Times')
    ax.legend(loc='upper left', ncols=1,
              fancybox=True, fontsize=7, prop={'family': 'Times New Roman'}) #bbox_to_anchor=(0.5, -0.1)

    plt.savefig(path_to_save, format='pdf', bbox_inches='tight')

    plt.show()


def viz_summary_files(kbs, summary_dir):
    summary_tables = []
    for kb_name, cs_name in kbs.items():
        # read the CSV file
        summary_file = summary_dir / f"{kb_name}_summary.csv"
        summary_df = pd.read_csv(summary_file)
        summary_tables.append(summary_df)

    # Plotting grouped by prompt_type
    categories = []
    values = defaultdict(list)
    std_devs = defaultdict(list)
    runtimes = defaultdict(list)

    for kb_name, cs_name in kbs.items():
        categories.append(cs_name[1])
    for summary in summary_tables:
        # loop each row in summary
        for index, row in summary.iterrows():
            config_name = row['config_name']
            if config_name == 'n\\\\textgreater1 CS':
                config_name = 'n>1 CS'
            if config_name == 'Extinction n\\\\textgreater1 CS':
                config_name = 'Extinction n>1 CS'

            values[config_name].append(row['found_cs'])
            runtimes[config_name].append(float(row['runtime[ms]']) / 60000.0)
            std_devs[config_name].append(row['std_dev'])

    # Calculate averages for each prompt_type
    max_css = {}
    for kb_name, cs_name in kbs.items():
        max_css[kb_name] = cs_name[0]

    title = f"Found Conflict Sets"
    plot(
        title=title,
        categories=categories,
        values=values,
        max_cs=max_css,
        std_devs=std_devs,
        ylabel='#identified conflict sets',
        path_to_save=summary_dir / f"summary_cs_plot.pdf"
    )

    title = f"Runtime"
    plot(
        title=title,
        categories=categories,
        values=runtimes,
        max_cs=None,
        std_devs=None,
        ylabel='runtime (minutes)',
        path_to_save=summary_dir / f"summary_runtime_plot.pdf"
    )


def viz_stats_files(all_config, summary_dir):
    export_names = all_config.export_names
    for kb_name, values in all_config.kbs.items():
        means = []
        stds = []
        for conf in all_config.eval_confs:
            # read the CSV file
            stats_file = summary_dir / f"{kb_name}_{conf}_average_stats.csv"
            std_file = summary_dir / f"{kb_name}_{conf}_std_stats.csv"
            mean_df = pd.read_csv(stats_file)
            std_df = pd.read_csv(std_file)
            means.append(mean_df)
            stds.append(std_df)
            # print(f"Mean DataFrame: {mean_df}")

        # Plot all three DataFrames in black and white
        plt.figure(figsize=(10, 5))

        x = means[0]['generation']  # mean_original_df['generation']

        for i, mean_df in enumerate(means):
            if i in all_config.excludes:
                continue

            std_dev = stds[i]['total_cs']
            plt.plot(x, mean_df['total_cs'],
                     label=export_names[i],
                     color=all_config.colors[i],
                     linestyle=all_config.linestyles[1],
                     # alpha=0.5,
                     marker=all_config.markers[i],
                     markersize=6)
            std_label = export_names[i] + ' std'
            plt.fill_between(
                x,
                mean_df['total_cs'] - std_dev,
                mean_df['total_cs'] + std_dev,
                alpha=0.3,                             # make shading translucent
                label= std_label
            )

        # Set the title and labels
        # plt.title(f'{kb_name} - Identified Conflict Sets', fontsize=12)
        plt.xlabel('generation', fontsize=10, fontname='Times New Roman')
        plt.ylabel('#identified conflict sets', fontsize=10, fontname='Times New Roman')
        plt.legend(fontsize=9,
                   ncols=2,
                   loc=all_config.legend_locs[kb_name],
                   prop={'family': 'Times New Roman'})
        plt.grid(True)

        # Increase the size of the numbers on the axes
        plt.tick_params(axis='both', which='major', labelsize=9)

        # Save the plot as a cropped PDF
        file_path = summary_dir / f"{kb_name}_stats_plot.pdf"
        plt.savefig(file_path, format='pdf', bbox_inches='tight')

        plt.show()


if __name__ == "__main__":
    main()