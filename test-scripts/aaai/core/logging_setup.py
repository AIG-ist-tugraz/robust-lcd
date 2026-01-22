#  Genetic Conflict Seeker
#
#  Copyright (c) 2023-2026
#
#  @author: Viet-Man Le (vietman.le@ist.tugraz.at)

#  Genetic Conflict Seeker
#
#
#  @author: Viet-Man Le (vietman.le@ist.tugraz.at)

# logging_setup.py
import logging
import logging.config
from pathlib import Path

def setup_logging(log_level: str, result_dir: Path):
    result_dir.mkdir(exist_ok=True, parents=True)
    config = {
        "version": 1,
        "disable_existing_loggers": False,
        "formatters": {
            "standard": {
                "format": "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
            }
        },
        "handlers": {
            "console": {
                "class": "logging.StreamHandler",
                "formatter": "standard",
                "level": log_level,
                "stream": "ext://sys.stdout"
            },
            # "console": {
            #     "()": "rich.logging.RichHandler",
            #     "level": log_level,
            #     # "rich_tracebacks": True,
            #     "show_time": True,
            #     "show_level": True,
            #     "markup": True
            # },
            "file": {
                "class": "logging.handlers.RotatingFileHandler",
                "formatter": "standard",
                "level": log_level,
                "filename": str(result_dir / "lazy_eval.log"),
                "maxBytes": 10*1024*1024,
                "backupCount": 5,
                "encoding": "utf-8"
            }
        },
        "root": {
            "level": log_level,
            "handlers": ["console", "file"]
        },
        "loggers": {
            # If you want a special logger name
            "LazyConflictDetectionEvaluation": {
                "level": log_level,
                "handlers": ["console", "file"],
                "propagate": False
            }
        }
    }
    logging.config.dictConfig(config)
    return logging.getLogger("LazyConflictDetectionEvaluation")