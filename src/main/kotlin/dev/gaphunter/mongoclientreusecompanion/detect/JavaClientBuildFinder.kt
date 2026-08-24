package dev.gaphunter.mongoclientreusecompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiNewExpression
import com.intellij.psi.util.PsiTreeUtil
import dev.gaphunter.mongoclientreusecompanion.model.ClientBuildHit

/**
 * Finds `new MongoClient(...)` (legacy driver) or
 * `MongoClients.create(...)` (modern driver, `com.mongodb.client`)
 * constructions written inside a non-constructor method body --
 * MongoDB's own documentation states "you will only need one instance
 * of class MongoClient even with multiple threads" and that the
 * "consequences of not following the singleton pattern are clear:
 * using multiple MongoClient instances may lead to too many open
 * connections in MongoDB". A MongoClient holds an internal connection
 * pool (default size 100) -- building one inside a regular method
 * means a brand new pool on every call.
 *
 * **v0.1 scope, stated honestly:** only the "build from scratch" shape
 * is flagged -- a client obtained by reference from an existing shared
 * instance/dependency injection is never flagged (correctly, since it
 * isn't the anti-pattern this plugin targets).
 */
object JavaClientBuildFinder {

    fun findAll(file: PsiFile): List<ClientBuildHit> {
        val hits = mutableListOf<ClientBuildHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitNewExpression(expression: PsiNewExpression) {
                super.visitNewExpression(expression)
                hitForDirectNew(expression)?.let { hits += it }
            }

            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                hitForClientsCreate(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitForDirectNew(newExpr: PsiNewExpression): ClientBuildHit? {
        val className = newExpr.classReference?.referenceName ?: return null
        if (className != "MongoClient") return null
        return hitIfNotInConstructor(newExpr)
    }

    private fun hitForClientsCreate(call: PsiMethodCallExpression): ClientBuildHit? {
        if (call.methodExpression.referenceName != "create") return null
        val qualifier = call.methodExpression.qualifierExpression ?: return null
        if (qualifier.text != "MongoClients") return null
        return hitIfNotInConstructor(call)
    }

    private fun hitIfNotInConstructor(element: PsiElement): ClientBuildHit? {
        val containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return null
        if (containingMethod.isConstructor) return null
        return ClientBuildHit(leafOf(element))
    }

    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
