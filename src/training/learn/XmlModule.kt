package training.learn

import com.yourkit.util.FileUtil.readBytes
import com.yourkit.util.FileUtil.readFileContentAsUtf8
import com.yourkit.util.Strings
import com.yourkit.util.Strings.createUTF8String
import org.jdom.Element
import org.jdom.JDOMException
import training.lang.LangManager
import training.lang.LangSupport
import training.learn.exceptons.BadLessonException
import training.learn.exceptons.BadModuleException
import training.learn.interfaces.Lesson
import training.learn.interfaces.Module
import training.learn.interfaces.ModuleType
import training.learn.lesson.Scenario
import training.learn.lesson.XmlLesson
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample
import training.util.DataLoader
import training.util.DataLoader.getResourceAsStream
import training.util.GenModuleXml
import training.util.GenModuleXml.*
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.util.*
import java.util.function.Consumer

/**
 * @author Sergey Karashevich
 */
class XmlModule(override val name: String, moduleXmlPath: String, private val root: Element): Module {

  override val description: String?

  //used for lessons filtered by LangManger chosen lang
  override var lessons: MutableList<Lesson> = ArrayList<Lesson>()
  override val sanitizedName: String
    get() = name.replace("[^\\dA-Za-z ]".toRegex(), "").replace("\\s+".toRegex(), "")
  override var id: String? = null
  override lateinit var moduleType: ModuleType

  private val allLessons = ArrayList<Lesson>()
  private val moduleUpdateListeners = ArrayList<ModuleUpdateListener>()

  val answersPath: String?
  var sdkType: ModuleSdkType?

  enum class ModuleSdkType {
    JAVA
  }

  init {
    val xroot = XRoot(root)
    description = xroot.valueNullable(MODULE_DESCRIPTION_ATTR)
    answersPath = xroot.valueNullable(MODULE_ANSWER_PATH_ATTR)
    id = xroot.valueNullable(MODULE_ID_ATTR)
    sdkType = getSdkTypeFromString(xroot.valueNullable(MODULE_SDK_TYPE))
    val fileTypeAttr = xroot.valueNotNull(MODULE_FILE_TYPE)
    moduleType = when {
      fileTypeAttr.toUpperCase() == ModuleType.SCRATCH.toString().toUpperCase() -> ModuleType.SCRATCH
      fileTypeAttr.toUpperCase() == ModuleType.PROJECT.toString().toUpperCase() -> ModuleType.PROJECT
      else -> throw BadModuleException("Unable to recognise ModuleType (should be SCRATCH or PROJECT)")
    }
    //path where module.xml is located and containing lesson dir
    val find = Regex("/[^/]*.xml").find(moduleXmlPath) ?: throw BadLessonException("Unable to parse a modules xml from '$moduleXmlPath'")
    val modulePath = moduleXmlPath.substring(0, find.range.start) + "/"
    initLessons(modulePath)
  }

  fun setMySdkType(mySdkType: ModuleSdkType?) {
    this.sdkType = mySdkType
  }

  private fun filterLessonsByCurrentLang(): MutableList<Lesson> {
    val langManager = LangManager.getInstance()
    if (langManager.isLangUndefined()) return allLessons
    return filterLessonByLang(langManager.getLangSupport())
  }

  override fun filterLessonByLang(langSupport: LangSupport): MutableList<Lesson> {
    return allLessons.filter { langSupport.acceptLang(it.lang) }.toMutableList()
  }

  override fun giveNotPassedLesson(): Lesson? {
    return lessons.firstOrNull { !it.passed }
  }

  override fun giveNotPassedAndNotOpenedLesson(): Lesson? {
    return lessons.firstOrNull { !it.passed && !it.isOpen }
  }

  override fun hasNotPassedLesson(): Boolean {
    return lessons.any { !it.passed }
  }

  override fun update() {
    lessons = filterLessonsByCurrentLang()
    moduleUpdateListeners.forEach(Consumer<ModuleUpdateListener> { it.onUpdate() })
  }

  private fun initLessons(modulePath: String) {

    if (root.getAttribute(MODULE_LESSONS_PATH_ATTR) != null) {

      //retrieve list of xml files inside lessonsPath directory
      val lessonsPath = modulePath + root.getAttribute(MODULE_LESSONS_PATH_ATTR).value

      for (lessonElement in root.children) {
        when (lessonElement.name) {
          MODULE_XML_LESSON_ELEMENT -> addXmlLesson(lessonElement, lessonsPath)
          MODULE_KT_LESSON_ELEMENT -> addKtLesson(lessonElement, lessonsPath)
          else -> throw BadModuleException("XmlModule file is corrupted or cannot be read properly")
        }
      }
    }
    lessons = filterLessonsByCurrentLang()
  }

  private fun addXmlLesson(lessonElement: Element, lessonsPath: String) {
    val lessonFilename = lessonElement.getAttributeValue(MODULE_LESSON_FILENAME_ATTR)
    val lessonPath = lessonsPath + lessonFilename
    try {
      val scenario = Scenario(lessonPath)
      val lesson = XmlLesson(scenario = scenario, lang = scenario.lang, module = this)
      allLessons.add(lesson)
    } catch (e: JDOMException) {
      //XmlLesson file is corrupted
      throw BadLessonException("Probably lesson file is corrupted: $lessonPath JDOMException:$e")
    } catch (e: IOException) {
      //XmlLesson file cannot be read
      throw BadLessonException("Probably lesson file cannot be read: " + lessonPath)
    }
  }

  private fun addKtLesson(lessonElement: Element, lessonsPath: String) {
    val lessonImplementation = lessonElement.getAttributeValue(MODULE_LESSON_IMPLEMENTATION_ATTR)
    val lessonSampleName = lessonElement.getAttributeValue(MODULE_LESSON_SAMPLE_ATTR)

    val lesson : Any
    if (lessonSampleName != null) {
      val lessonLanguage = lessonElement.getAttributeValue(MODULE_LESSON_LANGUAGE_ATTR)
      val lessonConstructor = Class.forName(lessonImplementation).
          getDeclaredConstructor(Module::class.java, String::class.java, LessonSample::class.java)

      val content = createUTF8String(readBytes(getResourceAsStream(lessonsPath + lessonSampleName)))
      val sample = parseLessonSample(content)
      lesson = lessonConstructor.newInstance(this, lessonLanguage, sample)
    }
    else {
      val lessonConstructor = Class.forName(lessonImplementation).
          getDeclaredConstructor(Module::class.java)
      lesson = lessonConstructor.newInstance(this)
    }
    if (lesson !is KLesson)
      throw BadLessonException("Field " + MODULE_LESSON_IMPLEMENTATION_ATTR + " should specify reference to existed class")
    allLessons.add(lesson)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as XmlModule

    if (name != other.name) return false
    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + (id?.hashCode() ?: 0)
    return result
  }

  inner class ModuleUpdateListener : EventListener {
    internal fun onUpdate() {}
  }

  companion object {

    @Throws(BadModuleException::class, BadLessonException::class, JDOMException::class, IOException::class, URISyntaxException::class)
    fun initModule(modulePath: String): XmlModule? {
      //load xml with lessons

      //Check DOM with XmlModule
      val root = getRootFromPath(modulePath)
      if (root.getAttribute(GenModuleXml.MODULE_NAME_ATTR) == null) return null
      val name = root.getAttribute(GenModuleXml.MODULE_NAME_ATTR).value

      return XmlModule(name, modulePath, root)

    }

    @Throws(JDOMException::class, IOException::class)
    fun getRootFromPath(pathToFile: String): Element {
      return DataLoader.getXmlRootElement(pathToFile)
    }
  }

  class XRoot(private val root: Element) {

    fun valueNotNull(attributeName: String): String {
      return root.getAttribute(attributeName)?.value ?: throw Exception("Unable to get attribute with name \"$attributeName\"")
    }

    fun valueNullable(attributeName: String): String? {
      return root.getAttribute(attributeName)?.value
    }
  }

}
