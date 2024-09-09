package org.radarbase.schema.specification.config

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import kotlin.io.path.relativeTo

interface PathMatcherConfig {
    val include: List<String>
    val exclude: List<String>

    fun pathMatcher(root: Path): PathMatcher = pathMatcher(root, FileSystems.getDefault())

    fun pathMatcher(root: Path, fs: FileSystem): PathMatcher = when {
        include.isNotEmpty() -> {
            val matchers = include.map { fs.getPathMatcher("glob:$it") }
            object : PathMatcher {
                override fun matches(path: Path): Boolean {
                    if (!Files.isRegularFile(path)) return false
                    val relativePath = path.relativeToAbsolutePath(root)
                    return matchers.any { it.matches(relativePath) }
                }

                override fun toString(): String = "PathMatcher{include=$include}"
            }
        }

        exclude.isNotEmpty() -> {
            val matchers = exclude.map { fs.getPathMatcher("glob:$it") }
            object : PathMatcher {
                override fun matches(path: Path): Boolean {
                    if (!Files.isRegularFile(path)) return false
                    val relativePath = path.relativeToAbsolutePath(root)
                    return matchers.none { it.matches(relativePath) }
                }

                override fun toString(): String = "PathMatcher{exclude=$exclude}"
            }
        }

        else -> object : PathMatcher {
            override fun matches(path: Path): Boolean {
                return Files.isRegularFile(path)
            }

            override fun toString(): String = "PathMatcher{all}"
        }
    }

    companion object {
        fun Path.relativeToAbsolutePath(absoluteBase: Path) = if (isAbsolute) {
            relativeTo(absoluteBase)
        } else {
            this
        }
    }
}
