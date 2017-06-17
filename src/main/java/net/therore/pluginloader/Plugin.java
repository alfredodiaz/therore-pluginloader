package net.therore.pluginloader;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

/**
 * Created by alfredo on 6/16/17.
 */

@Data
@AllArgsConstructor
public class Plugin {

    private File[] libraryDirectories;
    private String mainClass;

}
