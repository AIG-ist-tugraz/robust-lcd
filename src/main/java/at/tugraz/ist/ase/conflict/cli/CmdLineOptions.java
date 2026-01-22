/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.cli;

import at.tugraz.ist.ase.hiconfit.common.CmdLineOptionsBase;
import lombok.Getter;
import lombok.NonNull;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Command line options for the FastDiagPEvaluation application.
 */
public class CmdLineOptions extends CmdLineOptionsBase {

    @Getter
    @Option(name = "-cfg",
            aliases="--configuration-file",
            usage = "Specify the configuration file.")
    private String confFile = null;

    public CmdLineOptions(String banner, @NonNull String programTitle, String subtitle, @NonNull String usage) {
        super(banner, programTitle, subtitle, usage);

        parser = new CmdLineParser(this);
    }
}
