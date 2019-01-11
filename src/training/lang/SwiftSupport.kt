package training.lang

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.wm.ToolWindowAnchor
import training.project.ProjectUtils


/**
 * @author Sergey Karashevich
 */
class SwiftSupport : AbstractLangSupport() {
    private val acceptableLanguages = setOf("swift")
    override fun acceptLang(ext: String) = acceptableLanguages.contains(ext.toLowerCase())
    override val FILE_EXTENSION: String
        get() = "swift"


    override fun importLearnProject(): Project? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    
    override fun applyToProjectAfterConfigure(): (Project) -> Unit = {
    }

    override fun checkSdk(sdk: Sdk?, project: Project) {}

    override fun createProject(projectName: String, projectToClose: Project?): Project? {
        return ProjectUtils.importOrOpenProject("/learnProjects/"+ApplicationNamesInfo.getInstance().fullProductName.toLowerCase() + "_swift/LearnProjectSwift", "LearnProject")
    }
    override fun getToolWindowAnchor(): ToolWindowAnchor {
        return ToolWindowAnchor.RIGHT
    }
}