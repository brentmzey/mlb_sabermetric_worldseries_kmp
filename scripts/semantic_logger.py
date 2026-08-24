#!/usr/bin/env python3
"""
========================================================================================
🎨 Industry-Standard Semantic Logger for Data Engineering & ETL Pipelines
========================================================================================
Provides structured, ANSI-colorized, semantic log levels, execution stage dividers,
progress indicators, and table summary formatters with UTC ISO timestamps.
========================================================================================
"""
from __future__ import annotations

import sys
import time
import datetime
from enum import Enum
from typing import Any, Dict, Final, List, Mapping, Optional, Sequence, Tuple


class LogLevel(str, Enum):
    DEBUG = "DEBUG"
    INFO = "INFO "
    STAGE = "STAGE"
    METRIC = "METR "
    SUCCESS = "OK   "
    WARN = "WARN "
    ERROR = "ERROR"


class AnsiColor:
    RESET: Final[str] = "\033[0m"
    BOLD: Final[str] = "\033[1m"
    DIM: Final[str] = "\033[2m"
    UNDERLINE: Final[str] = "\033[4m"
    
    # Foreground colors
    BLACK: Final[str] = "\033[30m"
    RED: Final[str] = "\033[31m"
    GREEN: Final[str] = "\033[32m"
    YELLOW: Final[str] = "\033[33m"
    BLUE: Final[str] = "\033[34m"
    MAGENTA: Final[str] = "\033[35m"
    CYAN: Final[str] = "\033[36m"
    WHITE: Final[str] = "\033[37m"
    
    # Bright foreground
    BRIGHT_RED: Final[str] = "\033[91m"
    BRIGHT_GREEN: Final[str] = "\033[92m"
    BRIGHT_YELLOW: Final[str] = "\033[93m"
    BRIGHT_BLUE: Final[str] = "\033[94m"
    BRIGHT_MAGENTA: Final[str] = "\033[95m"
    BRIGHT_CYAN: Final[str] = "\033[96m"
    BRIGHT_WHITE: Final[str] = "\033[97m"
    
    # Background badges
    BG_BLUE: Final[str] = "\033[44m"
    BG_GREEN: Final[str] = "\033[42m"
    BG_MAGENTA: Final[str] = "\033[45m"
    BG_RED: Final[str] = "\033[41m"
    BG_YELLOW: Final[str] = "\033[43m"
    BG_CYAN: Final[str] = "\033[46m"


class SemanticLogger:
    """Enterprise-grade semantic logger for CLI & background worker execution."""

    def __init__(self, service_name: str = "MLB-ETL", use_color: bool = True) -> None:
        self.service_name: str = service_name
        self.use_color: bool = use_color and sys.stdout.isatty() if hasattr(sys.stdout, "isatty") else use_color

    def _format_timestamp(self) -> str:
        return datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%d %H:%M:%S.%f")[:-3] + " UTC"

    def _badge(self, level: LogLevel) -> str:
        if not self.use_color:
            return f"[{level.value}]"
        
        c = AnsiColor
        if level == LogLevel.INFO:
            return f"{c.CYAN}[INFO ]{c.RESET}"
        elif level == LogLevel.STAGE:
            return f"{c.BOLD}{c.MAGENTA}[STAGE]{c.RESET}"
        elif level == LogLevel.METRIC:
            return f"{c.BLUE}[METR ]{c.RESET}"
        elif level == LogLevel.SUCCESS:
            return f"{c.BOLD}{c.BRIGHT_GREEN}[OK   ]{c.RESET}"
        elif level == LogLevel.WARN:
            return f"{c.BOLD}{c.BRIGHT_YELLOW}[WARN ]{c.RESET}"
        elif level == LogLevel.ERROR:
            return f"{c.BOLD}{c.BRIGHT_RED}[ERROR]{c.RESET}"
        elif level == LogLevel.DEBUG:
            return f"{c.DIM}[DEBUG]{c.RESET}"
        return f"[{level.value}]"

    def log(self, level: LogLevel, message: str, **kwargs: Any) -> None:
        ts = self._format_timestamp()
        badge = self._badge(level)
        dim_ts = f"{AnsiColor.DIM}{ts}{AnsiColor.RESET}" if self.use_color else ts
        dim_svc = f"{AnsiColor.DIM}[{self.service_name}]{AnsiColor.RESET}" if self.use_color else f"[{self.service_name}]"
        
        extra_str = ""
        if kwargs:
            extra_parts = [f"{k}={v}" for k, v in kwargs.items()]
            extra_str = f" {AnsiColor.DIM}({', '.join(extra_parts)}){AnsiColor.RESET}" if self.use_color else f" ({', '.join(extra_parts)})"

        print(f"{dim_ts} {badge} {dim_svc} {message}{extra_str}", flush=True)

    def info(self, message: str, **kwargs: Any) -> None:
        self.log(LogLevel.INFO, message, **kwargs)

    def stage(self, stage_number: int, total_stages: int, title: str) -> None:
        c = AnsiColor
        bar = "═" * 70
        print("", flush=True)
        if self.use_color:
            print(f"{c.BOLD}{c.MAGENTA}┌{bar}┐{c.RESET}", flush=True)
            print(f"{c.BOLD}{c.MAGENTA}│  🚀 STAGE [{stage_number}/{total_stages}]: {title.ljust(54)}│{c.RESET}", flush=True)
            print(f"{c.BOLD}{c.MAGENTA}└{bar}┘{c.RESET}", flush=True)
        else:
            print(f"┌{bar}┐", flush=True)
            print(f"│  🚀 STAGE [{stage_number}/{total_stages}]: {title.ljust(54)}│", flush=True)
            print(f"└{bar}┘", flush=True)

    def success(self, message: str, **kwargs: Any) -> None:
        self.log(LogLevel.SUCCESS, message, **kwargs)

    def warn(self, message: str, **kwargs: Any) -> None:
        self.log(LogLevel.WARN, message, **kwargs)

    def error(self, message: str, **kwargs: Any) -> None:
        self.log(LogLevel.ERROR, message, **kwargs)

    def metric(self, key: str, value: Any, unit: str = "") -> None:
        val_str = f"{value}{unit}"
        if self.use_color:
            msg = f"{AnsiColor.BOLD}{key}{AnsiColor.RESET} = {AnsiColor.BRIGHT_CYAN}{val_str}{AnsiColor.RESET}"
        else:
            msg = f"{key} = {val_str}"
        self.log(LogLevel.METRIC, msg)

    def banner(self, title: str, subtitle: Optional[str] = None) -> None:
        c = AnsiColor
        w = 78
        line = "═" * w
        print("", flush=True)
        if self.use_color:
            print(f"{c.BOLD}{c.BRIGHT_BLUE}╔{line}╗{c.RESET}", flush=True)
            print(f"{c.BOLD}{c.BRIGHT_BLUE}║  ⚾ {title.ljust(w - 5)}║{c.RESET}", flush=True)
            if subtitle:
                print(f"{c.DIM}{c.BRIGHT_CYAN}║     {subtitle.ljust(w - 5)}║{c.RESET}", flush=True)
            print(f"{c.BOLD}{c.BRIGHT_BLUE}╚{line}╝{c.RESET}", flush=True)
        else:
            print(f"╔{line}╗", flush=True)
            print(f"║  ⚾ {title.ljust(w - 5)}║", flush=True)
            if subtitle:
                print(f"║     {subtitle.ljust(w - 5)}║", flush=True)
            print(f"╚{line}╝", flush=True)

    def summary_table(self, title: str, headers: Sequence[str], rows: Sequence[Sequence[Any]]) -> None:
        c = AnsiColor
        col_widths = [len(h) for h in headers]
        for row in rows:
            for i, val in enumerate(row):
                col_widths[i] = max(col_widths[i], len(str(val)))
        
        # Build dividers
        top = "┌" + "┬".join("─" * (w + 2) for w in col_widths) + "┐"
        mid = "├" + "┼".join("─" * (w + 2) for w in col_widths) + "┤"
        bot = "└" + "┴".join("─" * (w + 2) for w in col_widths) + "┘"

        print(f"\n{c.BOLD}📊 {title}:{c.RESET}" if self.use_color else f"\n📊 {title}:", flush=True)
        print(f"{c.DIM}{top}{c.RESET}" if self.use_color else top, flush=True)
        
        # Header row
        header_cells = [f" {headers[i].ljust(col_widths[i])} " for i in range(len(headers))]
        header_str = "│" + "│".join(header_cells) + "│"
        print(f"{c.BOLD}{header_str}{c.RESET}" if self.use_color else header_str, flush=True)
        print(f"{c.DIM}{mid}{c.RESET}" if self.use_color else mid, flush=True)

        # Data rows
        for row in rows:
            cells = [f" {str(row[i]).ljust(col_widths[i])} " for i in range(len(row))]
            row_str = "│" + "│".join(cells) + "│"
            print(row_str, flush=True)
        
        print(f"{c.DIM}{bot}{c.RESET}\n" if self.use_color else f"{bot}\n", flush=True)
