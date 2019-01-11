package training.learn.lesson.ruby

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext

class RubyNavigationLesson(module: Module) : KLesson("Basic Navigation", module, "ruby") {
  override val lessonContent: LessonContext.() -> Unit
    get() = {

      triggerTask("GotoDeclaration") {
        caret(5, 18)
        text("Use ${action(it)} to jump to the declaration of a class or interface.")
      }

      triggerTask("GotoClass") {
        text("Try to find a class with ${action(it)}")
      }
      triggerTask("QuickImplementations") {
        text("Write 'Us' and press ${action(it)} to see the definition of the selected class")
      }
      complete()
    }

  override val existedFile: String?
    get() = "app/controllers/users_controller.rb"
}
