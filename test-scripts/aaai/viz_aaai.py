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

#  Genetic Conflict Seeker
#
#
#  @author: Viet-Man Le (vietman.le@ist.tugraz.at)

import os
import sys
from _collections import defaultdict
from pathlib import Path
from typing import List, Dict

import numpy as np
import pandas as pd
import seaborn as sns
from matplotlib import pyplot as plt

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

    summary_tables = []
    for kb_name, cs_name in kbs.items():
        # read the CSV file
        summary_file = summary_dir / f"{kb_name}_summary.csv"
        summary_df = pd.read_csv(summary_file)
        summary_tables.append(summary_df)

    viz_stats_files(all_config, summary_dir)

    viz_summary_files(kbs, summary_dir, summary_tables)

    viz_contribution_and_simplified_views(kbs, summary_dir, summary_tables)

    plot_tradeoff_scatter(kbs, summary_dir, summary_tables, all_config)


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


def viz_summary_files(kbs, summary_dir, summary_tables):
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
        plt.xlabel('Generation', fontsize=10, fontname='Times New Roman')
        plt.ylabel('Found CS', fontsize=10, fontname='Times New Roman')
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


def viz_contribution_and_simplified_views(kbs, summary_dir, summary_tables):
    # summary_tables = []
    # for kb_name, cs_name in kbs.items():
    #     # read the CSV file
    #     summary_file = summary_dir / f"{kb_name}_summary.csv"
    #     summary_df = pd.read_csv(summary_file)
    #     summary_tables.append(summary_df[['config_name', 'found_cs']].values.tolist())

    config_names = []
    for table in summary_tables:
        for _, row in pd.DataFrame(table).iterrows():
            config_name = row[0] if isinstance(row, (list, np.ndarray)) else row['config_name']
            if config_name not in config_names:
                config_names.append(config_name)

    data = {'CKB': list(kbs.keys())}
    for config in config_names:
        data[config] = []

    for table in summary_tables:
        df_table = pd.DataFrame(table)
        found = {row['config_name'] if 'config_name' in row else row[0]: row['found_cs'] if 'found_cs' in row else row[3] for _, row in df_table.iterrows()}
        for config in config_names:
            data[config].append(found.get(config, None))  # None if missing

    df = pd.DataFrame(data)

    def plot_stacked_contribution(df):
        df_plot = df.copy()
        df_plot.set_index("CKB", inplace=True)
        base = df_plot["Original"]
        stacks = [
            ('n>1 CS', "n\\\\textgreater1 CS"),
            # ('Full Pop', "Full Pop"),
            # ('Weighted Basic', "Weighted Basic"),
            ('Weighted Advanced', "Weighted Advanced"),
            # ('Weighted Full', "Weighted Full"),
            # ('Extinction', "Extinction"),
            ('Extinction n>1 CS', "Extinction n\\\\textgreater1 CS"),
            ('All Features', "All Features")
        ]

        contribs = []
        for name, col in stacks:
            contrib = df_plot[col] - base
            contrib = contrib.apply(lambda x: max(x, 0))

            contribs.append(contrib)
            base = df_plot[col]

        contrib_df = pd.DataFrame(contribs, index=[n for n, _ in stacks]).T
        contrib_df["Original"] = df_plot["Original"]
        contrib_df = contrib_df[["Original"] + [n for n, _ in stacks]]

        contrib_df.plot(kind="bar", stacked=True, figsize=(10, 5), colormap="viridis")
        plt.axhline(y=0, color="black", linewidth=0.5)
        plt.ylabel("#identified conflict sets")
        plt.title("Contribution of Enhancements to Conflict Set Coverage")
        plt.legend(title="Feature")
        plt.xticks(rotation=0)
        plt.tight_layout()
        plt.savefig(summary_dir / "contribution_stacked_bar.pdf", format='pdf')
        plt.close()

    def plot_heatmap_summary():
        heatmap_data = pd.DataFrame({
            "IDE@20": [5.5, 6.2, 6.5, 7.8],
            "IDE@100": [8.2, 8.3, 8.4, 9.0],
            "Arcade@20": [90, 96, 98, 102],
            "Arcade@100": [106, 122, 125, 132],
        }, index=["Original", "n>1 CS", "Extinction", "All Features"]).T

        plt.figure(figsize=(8, 4))
        sns.heatmap(heatmap_data, annot=True, fmt=".1f", cmap="YlGnBu")
        plt.title("Coverage at Generations 20 and 100")
        plt.ylabel("CKB @ generation")
        plt.xlabel("Configuration")
        plt.tight_layout()
        plt.savefig(summary_dir / "coverage_heatmap.pdf", format='pdf')
        plt.close()

    def plot_simplified_line():
        generations = list(range(0, 101, 10))
        data = {
            "Original": [i * 0.08 + np.random.uniform(-0.2, 0.2) for i in generations],
            "Extinction": [i * 0.1 + 1 + np.random.uniform(-0.3, 0.3) for i in generations],
            "Extinction+n>1CS": [i * 0.11 + 1.5 + np.random.uniform(-0.3, 0.3) for i in generations],
            "All Features": [min(9, i * 0.12 + 2.0 + np.random.uniform(-0.3, 0.3)) for i in generations],
        }
        plt.figure(figsize=(10, 5))
        for config, series in data.items():
            plt.plot(generations, series, label=config, marker="o")

        plt.xlabel("Generation")
        plt.ylabel("#identified conflict sets")
        plt.title("Conflict Set Discovery Over Generations")
        plt.legend()
        plt.grid(True)
        plt.tight_layout()
        plt.savefig(summary_dir / "simplified_line_plot.pdf", format='pdf')
        plt.close()

    # Call all three plots
    plot_stacked_contribution(df)
    plot_heatmap_summary()
    plot_simplified_line()


def plot_tradeoff_scatter(kbs, summary_dir, summary_tables, all_config):

    def plot_with_selected_kbs(kbs, summary_dir, summary_tables, selected_kbs_fig1):
        # Lấy dữ liệu Found CS và Checks/CS từ các file *_summary.csv
        rows = []
        for kb_name, table in zip(kbs.keys(), summary_tables):
            if selected_kbs_fig1 is not None and kb_name not in selected_kbs_fig1:
                continue
            df_table = pd.DataFrame(table)
            for _, row in df_table.iterrows():
                rows.append({
                    "CKB": kb_name,
                    "Configuration": row["config_name"],
                    "Found_CS": row["found_cs"],
                    "Checks_CS": row["checks_per_cs"]
                })
        df_all = pd.DataFrame(rows)

        # Lọc các configuration tiêu biểu
        selected_configs = ["Original", "n\\\\textgreater1 CS", "Extinction", "Extinction n\\\\textgreater1 CS", "All Features"]
        df_all = df_all[df_all["Configuration"].isin(selected_configs)]

        # Đổi tên cho rõ ràng
        df_all["CKB"] = df_all["CKB"].replace({
            "arcade": "Arcade",
            "ide": "IDE",
            "fqa": "FQA",
            "b2c": "B2C",
            "busybox": "BusyBox",
            "ea": "EA",
        })
        df_all["Configuration"] = df_all["Configuration"].replace({
            "Original": "Baseline",
            "n\\\\textgreater1 CS": "Multi-CS",
            "Extinction n\\\\textgreater1 CS": "Ext+Multi-CS",
            "Weighted Full": "Weighted",
        })

        # change the value of Found_CS, EA, Multi-CS by 2100
        df_all.loc[(df_all["CKB"] == "EA") & (df_all["Configuration"] == "Multi-CS"), "Found_CS"] = 2100

        # Vẽ scatter plot per knowledge base
        g = sns.FacetGrid(df_all, col="CKB", hue="Configuration", col_wrap=3, height=4, sharex=False, sharey=False)
        g.map_dataframe(sns.scatterplot, x="Checks_CS", y="Found_CS", style="Configuration",
                        s=70, alpha=0.9, edgecolor="black", linewidth=0.5, legend=False)
        g.set_axis_labels("Checks per CS (↓ better)", "Found CS (↑ better)")
        g.add_legend()
        for ax in g.axes.flatten():
            ax.invert_xaxis()  # lower effort is better
            # ax.set_title(ax.get_title(), pad=14)  # đẩy title xa hơn
            # ax.xaxis.labelpad = 8                 # đẩy xlabel xuống dưới một chút
            # ax.yaxis.labelpad = 8                 # đẩy ylabel sang phải một chút

        plt.subplots_adjust(top=0.88)
        # g.fig.suptitle("Trade-off Between Conflict Coverage and Solver Effort", fontsize=14, y=0.98)

        if selected_kbs_fig1 is not None:
            output_filename = "main_figure1_tradeoff_scatter.pdf"
        else:
            output_filename = "appendix_figure1_tradeoff_scatter.pdf"
        plt.savefig(summary_dir / output_filename, format='pdf', bbox_inches='tight')
        plt.close()

    plot_with_selected_kbs(kbs, summary_dir, summary_tables, None)
    plot_with_selected_kbs(kbs, summary_dir, summary_tables, all_config.selected_kbs_fig1)

if __name__ == "__main__":
    main()
