/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.utils

import one.pkg.om.OmMain
import java.io.File
import java.util.jar.JarFile

object ClassScanner {
    fun scanClasses(packageName: String): List<Class<*>> {
        val plugin = OmMain.getInstance()
        val classes = mutableListOf<Class<*>>()
        val src = plugin.javaClass.protectionDomain.codeSource ?: return emptyList()

        val srcLoc = src.location
        val file = try {
            File(srcLoc.toURI())
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }

        if (file.isDirectory) {
            scanDir(file, packageName, classes)
        } else {
            JarFile(file).use { jar ->
                val entries = jar.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name
                    if (name.endsWith(".class")) {
                        val className = name.replace("/", ".").substring(0, name.length - 6)
                        if (className.startsWith(packageName)) {
                            try {
                                classes.add(Class.forName(className, false, plugin.javaClass.classLoader))
                            } catch (_: Throwable) {
                            }
                        }
                    }
                }
            }
        }
        return classes
    }

    private fun scanDir(dir: File, packageName: String, classes: MutableList<Class<*>>) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                scanDir(file, packageName, classes)
            } else if (file.name.endsWith(".class")) {
                val className = getClassNameFromFile(file)
                if (className != null && className.startsWith(packageName)) {
                    try {
                        classes.add(Class.forName(className, false, OmMain.getInstance().javaClass.classLoader))
                    } catch (_: Throwable) {
                    }
                }
            }
        }
    }

    private fun getClassNameFromFile(classFile: File): String? {
        val plugin = OmMain.getInstance()
        val src = plugin.javaClass.protectionDomain.codeSource ?: return null
        val srcLoc = src.location
        val rootFile = try {
            File(srcLoc.toURI())
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        val relativePath = classFile.absolutePath.removePrefix(rootFile.absolutePath)
            .removePrefix(File.separator)
            .replace(File.separator, ".")
            .removeSuffix(".class")

        return relativePath
    }
}
