#  Genetic Conflict Seeker
#
#  Copyright (c) 2023-2026
#
#  @author: Viet-Man Le (vietman.le@ist.tugraz.at)

"""
utils.py - Utility functions for processing evaluation results.

Provides functions for:
- Finding and loading summary/statistics files
- Computing aggregated statistics (mean, std)
- Adding derived columns (improvement percentages, rates)
- Formatting runtime values
"""

import glob
import numpy as np
import os
import pandas as pd
import re


# Function to find all summary files matching the provided list in the specified directory.
# Return list of paths to summary files.
def get_summary_files(directory, config_names):
    all_files = []
    for name in config_names:
        pattern = str(directory / f"summary_{name}.csv")
        # print(pattern)
        expanded_paths = glob.glob(pattern)
        if expanded_paths:
            all_files.extend(expanded_paths)
    return all_files

# Function to parse panda dataframe and calculate average of each column
def calculate_averages(df):
    averages = df[['total_populations', 'total_gens', 'found_cs', 'runtime[ms]']].mean()
    return pd.DataFrame(averages).transpose()

# Function to parse csv summary files and calculate the mean for each file and create a new overview dataframe
def process_all_summaries(summary_paths, totalMinCS=0):
    evaluation = pd.DataFrame()
    for summary in summary_paths:
        summary_df = pd.read_csv(summary)
        averages_df = calculate_averages(summary_df)
        config_name = re.search(r'summary_(.*).csv', summary).group(1)
        # calculate and add the variance and standard deviation to the averages_df
        variance = round(summary_df['found_cs'].var(), 3)
        std_dev = round(summary_df['found_cs'].std(), 3)
        averages_df['variance'] = variance
        averages_df['std_dev'] = std_dev
        # calculate the identification rate by dividing the number of found CS by the total number of CS
        if totalMinCS > 0:
            averages_df['identification_rate'] = averages_df['found_cs'] / totalMinCS
            averages_df['identification_rate'] = averages_df['identification_rate'].round(3)
            # count the total number of values in column 'found_cs' in summary_df that are equal to totalMinCS
            averages_df['found_all_cs'] = np.count_nonzero(summary_df['found_cs'] == totalMinCS)
        averages_df['config_name'] = config_name
        evaluation = pd.concat([evaluation, averages_df], ignore_index=True)
    # Move the 'config_name' column to the first position
    config_name = evaluation.pop('config_name')
    evaluation.insert(0, 'config_name', config_name)
    return evaluation

# Function to add a column to the dataframe containing the runtime in a human-readable format
def runtime_to_string(df):
    df['runtime[m:s:ms]'] = df['runtime[ms]'].apply(
        lambda x: f"{int(x // 60000)}:{int((x % 60000) // 1000)}:{int(x % 1000)}")
    return df

def add_consistency_checks_per_identified_cs(df, population_size=100):
    df['checks_per_cs'] = df['total_gens'] * population_size / df['found_cs']
    df['checks_per_cs'] = df['checks_per_cs'].round(2)
    return df

# Function to add a column showing the relational number of identified CS per time unit
def add_identified_cs_per_time_column(df, time_unit='m'):
    if time_unit == 'm':
        df['cs_per_min'] = df['found_cs'] / (df['runtime[ms]'] / 1000 / 60)
        df['cs_per_min'] = df['cs_per_min'].round(2)
    elif time_unit == 's':
        df['cs_per_sec'] = df['found_cs'] / (df['runtime[ms]'] / 1000)
        df['cs_per_sec'] = df['cs_per_sec'].round(2)
    elif time_unit == 'ms':
        df['cs_per_ms'] = df['found_cs'] / df['runtime[ms]']
        df['cs_per_ms'] = df['cs_per_ms'].round(2)
    return df

def add_improvement_column(df, name, target_column, base_config_name, inPercent=True, less_is_better=False):
    base_value = df.loc[df['config_name'] == base_config_name, target_column].values[0]
    if inPercent:
        if less_is_better:
            df[name] = ((base_value - df[target_column]) / base_value) * 100
        else:
            df[name] = ((df[target_column] - base_value) / base_value) * 100
    else:
        if less_is_better:
            df[name] = base_value - df[target_column]
        else:
            df[name] = df[target_column] - base_value
    df[name] = df[name].round(2)

    # Insert the new column after the target column
    target_index = df.columns.get_loc(target_column)
    cols = df.columns.tolist()
    cols.insert(target_index + 1, cols.pop(cols.index(name)))
    df = df[cols]

    return df

# Function to summarize the evaluation (statistics) files and generate a superfile
# Parameter is the directory containing the statistics files and the expected length,
# in case the algorithm did reach the maximum number of generations
# Return a dataframe containing the (mean) summarized data
def calculate_average_stats(directory, expected_length=100):
    if not os.path.exists(directory):
        print(f"Directory {directory} does not exist.")
        return None
    # Get all summary files in the directory (directory/stats_N.csv)
    stat_files = glob.glob(f"{directory}/stats_*.csv")
    if not stat_files or len(stat_files) == 0:
        print("No statistics files found in the directory.")
        return None
    # read all statistics files and calculate the means over all files
    dataframes = [pd.read_csv(file) for file in stat_files]

    # calculate average length of all dataframes and round down
    avg_length = int(sum([len(df) for df in dataframes]) / len(dataframes))
    # get the length of the longest dataframe
    max_length = max([len(df) for df in dataframes])
    # get the length of the shortest dataframe
    min_length = min([len(df) for df in dataframes])

    for i, df in enumerate(dataframes):
        # if len(dataframes[i]) < expected_length:
        if dataframes[i]['generation'].iloc[-1] < expected_length:
            # Fill missing values in 'total_cs' with the last available value
            last_value = dataframes[i]['total_cs'].iloc[-1]
            dataframes[i] = dataframes[i].reindex(range(avg_length), method=None)
            # dataframes[i]['total_cs'].fillna(last_value, inplace=True)
            dataframes[i].fillna({'total_cs': last_value}, inplace=True)
            # change the 'generation' column to count from 0 to the end of the table
            dataframes[i]['generation'] = range(len(dataframes[i]))

    # Concatenate all dataframes
    merged_df = pd.concat(dataframes)

    # Group by the generation column and calculate the mean for each group
    mean_df = merged_df.groupby('generation').mean().reset_index()

    if len(mean_df) < expected_length:
        mean_df = mean_df.reindex(range(expected_length))

    return mean_df


def calculate_std_stats(directory, expected_length=100):
    if not os.path.exists(directory):
        print(f"Directory {directory} does not exist.")
        return None
    # Get all summary files in the directory (directory/stats_N.csv)
    stat_files = glob.glob(f"{directory}/stats_*.csv")
    if not stat_files or len(stat_files) == 0:
        print("No statistics files found in the directory.")
        return None
    # read all statistics files and calculate the means over all files
    dataframes = [pd.read_csv(file) for file in stat_files]

    # calculate average length of all dataframes and round down
    avg_length = int(sum([len(df) for df in dataframes]) / len(dataframes))
    # get the length of the longest dataframe
    max_length = max([len(df) for df in dataframes])
    # get the length of the shortest dataframe
    min_length = min([len(df) for df in dataframes])

    for i, df in enumerate(dataframes):
        # if len(dataframes[i]) < expected_length:
        if dataframes[i]['generation'].iloc[-1] < expected_length:
            # Fill missing values in 'total_cs' with the last available value
            last_value = dataframes[i]['total_cs'].iloc[-1]
            dataframes[i] = dataframes[i].reindex(range(avg_length), method=None)
            # dataframes[i]['total_cs'].fillna(last_value, inplace=True)
            dataframes[i].fillna({'total_cs': last_value}, inplace=True)
            # change the 'generation' column to count from 0 to the end of the table
            dataframes[i]['generation'] = range(len(dataframes[i]))

    # Concatenate all dataframes
    merged_df = pd.concat(dataframes)

    # Group by the generation column and calculate the mean for each group
    std_df = merged_df.groupby('generation').std().reset_index()

    if len(std_df) < expected_length:
        std_df = std_df.reindex(range(expected_length))

    return std_df