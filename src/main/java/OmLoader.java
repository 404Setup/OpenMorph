/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class OmLoader implements PluginLoader {
    final String kotlinVersion = "2.3.0";

    @Override
    public void classloader(PluginClasspathBuilder builder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        addDependency(resolver, "org.jetbrains.kotlin:kotlin-stdlib:" + kotlinVersion);
        addDependency(resolver, "org.jetbrains.kotlin:kotlin-reflect:" + kotlinVersion);
        addDependency(resolver, "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2");
        addDependency(resolver, "org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0");
        addDependency(resolver, "org.jetbrains.kotlinx:atomicfu:0.29.0");
        addDependency(resolver, "org.jetbrains.kotlinx:kotlinx-io-core:0.8.2");
        addDependency(resolver, "org.jetbrains.kotlinx:kotlinx-datetime:0.7.1");
        addDependency(resolver, "com.github.avro-kotlin.avro4k:avro4k-core:2.9.0");
        resolver.addRepository(
                new RemoteRepository.Builder(
                        "paper",
                        "default",
                        "https://repo.papermc.io/repository/maven-public/"
                ).build()
        );
        builder.addLibrary(resolver);
    }

    public void addDependency(MavenLibraryResolver it, String dependency) {
        it.addDependency(new Dependency(new DefaultArtifact(dependency), null));
    }
}
