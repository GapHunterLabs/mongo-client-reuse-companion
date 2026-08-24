package dev.gaphunter.mongoclientreusecompanion.model

import com.intellij.psi.PsiElement

/** One `new MongoClient(...)`/`MongoClients.create(...)` call site built inside a non-constructor method. */
data class ClientBuildHit(val callElement: PsiElement)
