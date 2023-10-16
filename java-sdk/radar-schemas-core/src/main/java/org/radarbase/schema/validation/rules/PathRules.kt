package org.radarbase.schema.validation.rules

import java.nio.file.Path
import kotlin.io.path.extension

/** Validator that checks if a path has given extension. */
fun hasExtension(extension: String) = Validator<Path> { path ->
    if (!path.extension.equals(extension, ignoreCase = true)) {
        raise("Path $path does not have extension $extension")
    }
}
