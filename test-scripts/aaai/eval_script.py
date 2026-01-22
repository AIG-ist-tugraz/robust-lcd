#!/usr/bin/env python3
"""
Script to run evaluations from a list of configuration files.
"""
#  Genetic Conflict Seeker
#
#  Copyright (c) 2026
#
#  @author: Viet-Man Le (vietman.le@ist.tugraz.at)

#  Genetic Conflict Seeker
#
#
#  @author: Viet-Man Le (vietman.le@ist.tugraz.at)

import datetime
import glob
import logging
import os
import subprocess
import sys
import tempfile
import uuid
from pathlib import Path
from typing import Dict, List, Optional, Tuple

from core.conf import RunnerConfig, RunnerConfs, read_gc_seeker_conf, convert_gc_seeker_conf_to_cfg, \
    read_config
from core.logging_setup import setup_logging

# This is a workaround to set the root project folder as the current working directory
ROOT_PROJECT_FOLDER = Path(__file__).resolve().parent.parent.parent
os.chdir(ROOT_PROJECT_FOLDER)
sys.path.insert(0, os.getcwd())

class EvalRunner:
    """Evaluation Runner - manages the execution of evaluations with"""

    def __init__(
        self,
        gc_seeker_path: Path,
        kb_name: str,
        conf_dir: Path,
        result_dir: Path,
        runs_per_config: int = 5,
        log_level: int = logging.INFO,
        confs: Optional[RunnerConfs] = None
    ) -> None:
        """
        Initialize the test runner with paths and configuration options.
    
        Args:
            gc_seeker_path: Path to the gc_seeker JAR file
            conf_dir: Directory containing configuration files
            result_dir: Directory to store results
            runs_per_config: Number of runs per configuration
            log_level: Logging level
        """
        self.gc_seeker = gc_seeker_path
        self.kb_name = kb_name
        self.conf_dir = conf_dir
        self.result_dir = result_dir
        self.runs_per_config = runs_per_config

        # create the result directory if it does not exist
        if not self.result_dir.exists():
            self.result_dir.mkdir(parents=True, exist_ok=True)

        # Configure logging
        # self.logger = self._setup_logging(log_level)
        self.logger = setup_logging(log_level, self.result_dir)
        
        # Default configuration names
        self.confs = confs

    def resolve_conf_files(self) -> List[Path]:
        """
        Resolve configuration file paths, handling wildcards.
        
        Returns:
            List of resolved Path objects
        """
        conf_file_paths = []
        
        for name in self.confs.include:
            pattern = str(self.conf_dir / name)
            expanded_paths = glob.glob(pattern)
            
            if expanded_paths:
                conf_file_paths.extend([Path(p) for p in expanded_paths])
            else:
                conf_file_paths.append(self.conf_dir / name)
        
        return conf_file_paths

    def validate_config_files(self, config_files: List[Path]) -> bool:
        """
        Validate that all configuration files exist.
        
        Args:
            config_files: List of configuration file paths
            
        Returns:
            True if all files exist, False otherwise
        """
        missing_files = [path for path in config_files if not path.exists()]
        
        if missing_files:
            for path in missing_files:
                self.logger.error(f"Config file not found: {path}")
            return False
        
        self.logger.info("Config files found:")
        for path in config_files:
            self.logger.info(f"  {path}")
        
        return True

    def run_conf(self, conf: Dict) -> Tuple[bool, Optional[str]]:
        """
        Run a single configuration by creating a temporary config file and
        executing the JAR with it.
        
        Args:
            conf: Dictionary of configuration key-value pairs
            
        Returns:
            Tuple of (success, error_message)
        """
        # Use a temporary file for the cfg file
        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.cfg') as temp_file:
            temp_path = Path(temp_file.name)
            
            # Write configuration to a temporary file
            try:
                for key, value in conf.items():
                    temp_file.write(f"{key}={value}\n")
                    # print(f"{key}={value}")
            except Exception as e:
                self.logger.error(f"Error writing config file: {e}")
                temp_path.unlink(missing_ok=True)
                return False, f"Failed to write config: {str(e)}"

        # Run process
        try:
            result = subprocess.run(
                ["java", "-jar", str(self.gc_seeker), "-cfg", str(temp_path)],
                capture_output=True,
                text=True,
                check=True
            )

            # Log success but keep stdout/stderr at debug level
            self.logger.debug(f"Process stdout: {result.stdout}")
            self.logger.debug(f"Process stderr: {result.stderr}")
            return True, None

        except subprocess.CalledProcessError as e:
            error_msg = (
                f"Error executing JAR file: {e}\n"
                f"Output: {e.stdout}\n"
                f"Error: {e.stderr}"
            )
            self.logger.error(error_msg)
            return False, error_msg

        finally:
            # Always clean up the temporary file
            temp_path.unlink(missing_ok=True)

    def process_conf(self, conf_path: Path) -> bool:
        """
        Process a single configuration file by running multiple iterations.
        
        Args:
            conf_path: Path to the configuration file
            
        Returns:
            True if all iterations completed successfully, False otherwise
        """
        name = conf_path.stem
        self.logger.info(f"Running configuration: {name}")
        
        try:
            # Load base configuration
            # conf = self.load_conf(conf_path)
            conf_dict = convert_gc_seeker_conf_to_cfg(read_gc_seeker_conf(conf_path))
            # change kbpath
            # conf_dict['kbPath'] = str(self.kb_name)

            summary_path = self.result_dir / f"summary_{name}.csv"
            conf_dict['summaryPath'] = str(summary_path)
            
            # Create a directory for this configuration
            conf_dir = self.result_dir / name
            conf_dir.mkdir(exist_ok=True)
            
            success_count = 0
            
            # Run iterations
            for i in range(self.runs_per_config):

                idx = str(uuid.uuid4())
                stats_file = conf_dir / f"stats_{idx}.csv"
                result_file = conf_dir / f"result_{idx}.txt"
                cs_file = conf_dir / f"result_{idx}.da"
                cs_with_cf_file = conf_dir / f"result_with_cf_{idx}.da"
                
                # Prepare a cfg file for this iteration
                cfg = conf_dict.copy()
                cfg['statisticsPath'] = str(stats_file)
                cfg['resultPath'] = str(result_file)
                cfg['allCSWithoutCFPath'] = str(cs_file)
                
                if 'cfInConflicts' not in conf_dict or conf_dict['cfInConflicts'] != 'no':
                    cfg['allCSPath'] = str(cs_with_cf_file)
                
                # Run this iteration
                time = datetime.datetime.now().strftime("%H:%M:%S")
                self.logger.info(f"Running configuration '{name}' iteration {i}. [{time}]")
                
                success, error = self.run_conf(cfg)
                if success:
                    success_count += 1
                else:
                    self.logger.error(f"Failed to run iteration {i} of {name}: {error}")
            
            self.logger.info(
                f"Configuration '{name}' completed. "
                f"{success_count}/{self.runs_per_config} iterations successful."
            )
            return success_count == self.runs_per_config
            
        except Exception as e:
            self.logger.error(f"Error processing configuration {name}: {e}", exc_info=True)
            return False

    def run(self) -> int:
        """
        Main execution method - run all tests.
        
        Returns:
            Exit code (0 for success, non-zero for error)
        """
        try:
            self.logger.info("Starting evaluation run...")
            self.logger.info(f"GC Seeker JAR: {self.gc_seeker}")
            self.logger.info(f"Knowledge Base: {self.kb_name}")
            self.logger.info(f"Conf directory: {self.conf_dir}")
            self.logger.info(f"Result directory: {self.result_dir}")
            self.logger.info(f"Runs per configuration: {self.runs_per_config}")
            
            # Resolve and validate configuration files
            conf_files = self.resolve_conf_files()
            if not self.validate_config_files(conf_files):
                self.logger.error("Configuration validation failed")
                return 1
            
            success_count = 0

            for conf_path in conf_files:
                if self.process_conf(conf_path):
                    success_count += 1
            
            # Report results
            self.logger.info(
                f"Configurations completed: "
                f"{success_count}/{len(conf_files)} successful"
            )
            
            return 0 if success_count == len(conf_files) else 1
            
        except Exception as e:
            self.logger.error(f"An error occurred: {e}", exc_info=True)
            return 1


def main():
    """Main entry point"""
    all_config = read_config(prog="eval_script.py",
                             description="Lazy Conflict Detection Evaluation",
                             root=ROOT_PROJECT_FOLDER,
                             config_class=RunnerConfig)

    # Determine log level
    log_level = logging.DEBUG if all_config.verbose else logging.INFO

    working_dir = ROOT_PROJECT_FOLDER / all_config.working_dir / all_config.kb_name
    # Create and run the test runner
    runner = EvalRunner(
        gc_seeker_path=ROOT_PROJECT_FOLDER / all_config.gc_seeker,
        kb_name=all_config.kb_name,
        conf_dir=working_dir / all_config.conf_dirname,
        result_dir=working_dir / all_config.result_dirname,
        runs_per_config=all_config.runs_per_config,
        confs=all_config.confs,
        log_level=log_level
    )

    exit_code = runner.run()
    sys.exit(exit_code)


if __name__ == "__main__":
    main()
