package dev.gaphunter.mongoclientreusecompanion.detect

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import dev.gaphunter.mongoclientreusecompanion.model.ClientBuildHit
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/** Kotlin counterpart of [JavaClientBuildFinder]. */
object KotlinClientBuildFinder {

    fun findAll(file: PsiFile): List<ClientBuildHit> {
        if (file !is KtFile) return emptyList()
        val hits = mutableListOf<ClientBuildHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                super.visitCallExpression(expression)
                hitForDirectConstruct(expression)?.let { hits += it }
            }

            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                super.visitDotQualifiedExpression(expression)
                hitForClientsCreate(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitForDirectConstruct(call: KtCallExpression): ClientBuildHit? {
        if (call.calleeExpression?.text != "MongoClient") return null
        return hitIfNotInConstructor(call)
    }

    private fun hitForClientsCreate(expression: KtDotQualifiedExpression): ClientBuildHit? {
        val call = expression.selectorExpression as? KtCallExpression ?: return null
        if (call.calleeExpression?.text != "create") return null
        if (expression.receiverExpression.text != "MongoClients") return null
        return hitIfNotInConstructor(expression)
    }

    private fun hitIfNotInConstructor(element: PsiElement): ClientBuildHit? {
        if (PsiTreeUtil.getParentOfType(element, KtConstructor::class.java) != null) return null
        if (PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java) == null) return null
        return ClientBuildHit(leafOf(element))
    }

    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
