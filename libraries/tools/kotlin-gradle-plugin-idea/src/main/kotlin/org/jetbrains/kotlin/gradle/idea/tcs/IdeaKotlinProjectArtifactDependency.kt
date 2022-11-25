/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.tcs

import org.jetbrains.kotlin.tooling.core.MutableExtras
import org.jetbrains.kotlin.tooling.core.mutableExtrasOf

data class IdeaKotlinProjectArtifactDependency(
    val type: IdeaKotlinSourceDependency.Type,
    override val coordinates: IdeaKotlinProjectArtifactCoordinates,
    override val extras: MutableExtras = mutableExtrasOf()
) : IdeaKotlinDependency {

    @IdeaKotlinService
    fun interface Resolver {
        fun resolve(dependency: IdeaKotlinProjectArtifactDependency): IdeaKotlinSourceDependency?

        companion object {
            fun byName(resolveSourceSetName: (IdeaKotlinProjectArtifactDependency) -> String?): Resolver {
                return DefaultProjectArtifactDependencyResolver(resolveSourceSetName)
            }

            fun composite(vararg resolver: Resolver?) =
                composite(resolver.toList())

            fun composite(resolvers: Iterable<Resolver?>): Resolver =
                CompositeProjectArtifactDependencyResolver(resolvers.filterNotNull())

        }
    }

    internal companion object {
        const val serialVersionUID = 0L
    }
}

fun IdeaKotlinProjectArtifactDependency.resolved(resolver: IdeaKotlinProjectArtifactDependency.Resolver): IdeaKotlinSourceDependency? {
    return resolver.resolve(this)
}

private class DefaultProjectArtifactDependencyResolver(
    private val resolveSourceSetName: (IdeaKotlinProjectArtifactDependency) -> String?
) : IdeaKotlinProjectArtifactDependency.Resolver {
    override fun resolve(dependency: IdeaKotlinProjectArtifactDependency): IdeaKotlinSourceDependency? {
        return IdeaKotlinSourceDependency(
            type = dependency.type,
            coordinates = IdeaKotlinSourceCoordinates(
                project = dependency.coordinates.project,
                sourceSetName = resolveSourceSetName(dependency) ?: return null
            ),
            extras = dependency.extras
        )
    }
}

private class CompositeProjectArtifactDependencyResolver(
    private val resolvers: List<IdeaKotlinProjectArtifactDependency.Resolver>
) : IdeaKotlinProjectArtifactDependency.Resolver {
    override fun resolve(dependency: IdeaKotlinProjectArtifactDependency): IdeaKotlinSourceDependency? {
        return resolvers.firstNotNullOfOrNull { resolver -> resolver.resolve(dependency) }
    }
}
