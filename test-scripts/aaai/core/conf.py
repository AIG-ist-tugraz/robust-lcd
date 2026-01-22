#  Genetic Conflict Seeker
#
#  Copyright (c) 2023-2026
#
#  @author: Viet-Man Le (vietman.le@ist.tugraz.at)

"""
conf.py - Configuration models and loaders for evaluation scripts.

Defines Pydantic models for:
- GeneticConflictSeeker configuration (GA parameters, weighting, extinction)
- Evaluation runner configuration
- Summary and visualization configuration
- CSV to LaTeX conversion configuration

Also provides TOML configuration file readers.
"""

import argparse
import os
import sys
import tomli
from pathlib import Path, PurePath
from pydantic import BaseModel, Field
from typing import Optional, List, Dict

# conf.py

ENABLED_FEATURES_KEY = "enabled_features"
INCLUDE_KEY = "include"
ALL_FEATURES = ("base", "extinction", "weighting")


# ─── Models for GC Seeker ───────────────────────────────────────────────────────

class KbConfig(BaseModel):
    nameKB: str
    kbPath: str

class OutputConfig(BaseModel):
    printResult: bool = False

class GAConfig(BaseModel):
    noPreferenceProbability: float = Field(0.7, ge=0.0, le=1.0)
    mutationProbability:     float = Field(0.1, ge=0.0, le=1.0)
    maxFeaturesInUR:         int   = Field(100, ge=10)

class PopulationConfig(BaseModel):
    populationSize:    int = Field(100, ge=1)
    maxNumGenerations: int = Field(100, ge=1)

class CsConfig(BaseModel):
    maxNumConflicts:        int  = Field(1, ge=1)
    limitParentsToResolved: bool

class BaseConfig(BaseModel):
    kb:         KbConfig
    output:     OutputConfig
    ga:         GAConfig
    population: PopulationConfig
    cs:         CsConfig

class Extinction(BaseModel):
    extinctAfterXTimesNoConflict: int = 5
    stopAfterXExtinctions:        int = 5

class Weighting(BaseModel):
    weightedConflicts:           bool = False
    avoidSameOriginalConflict:   bool = False
    weightedCrossover:           bool = False
    weightedCrossoverFactor:     int  = Field(2, ge=1)

class GCSeekerConfig(BaseModel):
    # If present, only these top-level tables will be kept
    enabled_features: Optional[List[str]] = Field(
        default=None,
        description="List of sections to load, e.g. ['base','extinction','weighting']"
    )
    base:       Optional[BaseConfig]
    extinction: Optional[Extinction]
    weighting:  Optional[Weighting]

# ─── Models for Evaluations ───────────────────────────────────────────────────────

class RunnerConfs(BaseModel):
    include: List[str]

class RunnerConfig(BaseModel):
    gc_seeker:      Path
    working_dir:    Path
    kb_name:        str
    conf_dirname:   str
    result_dirname: str
    runs_per_config:int = 1
    verbose:        bool = False
    confs:          RunnerConfs

# ─── Models for Summary ───────────────────────────────────────────────────────

class SummaryConfig(BaseModel):
    working_dir:    Path
    kbs: Dict[str, int] = Field(default_factory=dict)
    eval_confs : List[str] = Field(default_factory=list)
    export_names: List[str] = Field(default_factory=list)
    result_dirname: str
    summary_dirname: str

# ─── Models for Viz ───────────────────────────────────────────────────────

class VizConfig(BaseModel):
    working_dir:    Path
    kbs: Dict[str, List] = Field(default_factory=dict)
    selected_kbs_fig1: List[str] = Field(default_factory=list)
    eval_confs : List[str] = Field(default_factory=list)
    export_names: List[str] = Field(default_factory=list)
    result_dirname: str
    summary_dirname: str
    excludes: List[int] = Field(default_factory=list)
    legend_locs: Dict[str, str] = Field(default_factory=dict)
    colors: List[str] = Field(default_factory=list)
    markers: List[str] = Field(default_factory=list)
    linestyles: List[str] = Field(default_factory=list)

# ─── Models for CSV2Latex ───────────────────────────────────────────────────────

class CSV2LatexConfig(BaseModel):
    input_csv:    Path
    output_tex:   Path
    caption:      str = ""
    label:        str = ""
    included_columns: List[str] = Field(default_factory=list)
    headers: Dict[str, str] = Field(default_factory=dict)

# ─── Loader ───────────────────────────────────────────────────────────────────

def read_gc_seeker_conf(path: Path) -> GCSeekerConfig:
    raw = tomli.loads(path.read_text())

    # 1) support include of common.toml
    if include := raw.pop(INCLUDE_KEY, None):
        base_path = path.parent / include
        base_raw  = tomli.loads(base_path.read_text())
        # user overrides win
        # base_raw.update({k: v for k, v in base_raw.items() if k in base_raw})

        # merge
        for key, value in raw.items():
            if key == 'base':
                # merge base with base_raw
                for sub_key, sub_value in value.items():
                    base_raw['base'][sub_key] = sub_value
            else:
                base_raw[key] = value

        raw = base_raw

    # 2) drop any feature not in enabled_features
    if feats := raw.get(ENABLED_FEATURES_KEY):
        allowed = set(feats)
        for top in ALL_FEATURES:
            if top not in allowed:
                raw[top] = None

    # 3) validate and return
    return GCSeekerConfig.model_validate(raw)

def convert_gc_seeker_conf_to_cfg(conf: GCSeekerConfig) -> dict:
    # Helper function to flatten nested dictionaries
    def flatten_dict(d, parent_key='', sep='.'):
        items = []
        for k, v in d.items():
            new_key = f"{k}" if parent_key else k
            if isinstance(v, dict):
                items.extend(flatten_dict(v, new_key, sep=sep).items())
            else:
                items.append((new_key, v))
        return dict(items)

    # 1) convert to dict
    conf_dict = conf.model_dump()

    # 2) remove None values
    for key, value in list(conf_dict.items()):
        if value is None:
            del conf_dict[key]

    # 3) remove enabled_features key
    conf_dict.pop(ENABLED_FEATURES_KEY, None)

    # 4) flatten the dictionary
    conf_dict = flatten_dict(conf_dict)

    # 5) convert "False" to "no" and "True" to "yes"
    for key, value in conf_dict.items():
        if isinstance(value, bool):
            conf_dict[key] = "yes" if value else "no"

    return conf_dict

def read_config_by_type(path: Path, config_class: type):
    # assert config_class in (RunnerConfig, SummaryConfig, VizConfig)
    if config_class not in (RunnerConfig, SummaryConfig, VizConfig, CSV2LatexConfig):
        raise ValueError(f"Invalid config class: {config_class}")

    raw = tomli.loads(path.read_text())
    if config_class == RunnerConfig:
        runner = raw.get("runner", {})

        # ensure we always have a confs table (will error if missing include)
        runner.setdefault("confs", {})
    else:
        runner = raw

    return config_class.model_validate(runner)

def read_config(prog: str, description: str, root: PurePath, config_class: type) -> RunnerConfig:
    """ Read the config file """
    parser = argparse.ArgumentParser(prog=prog,
                                     description=description)
    parser.add_argument('-cfg', '--cfg', action='store', type=str, help='path to the config file')
    args = parser.parse_args()

    # check if the config file exists
    if os.path.exists(root / args.cfg):
        print(f"Configuration file: {root / args.cfg}")
    else:
        print("Configuration file not found")
        sys.exit(1)
    return read_config_by_type(root / args.cfg, config_class)