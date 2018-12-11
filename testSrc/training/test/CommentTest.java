package training.test;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import training.learn.CourseManager;
import training.learn.interfaces.Lesson;

public class CommentTest extends LightPlatformCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        PluginManagerCore.disablePlugin("Pythonid");
        super.setUp();
    }

    public void testComment() {
        //PluginManagerCore.disablePlugin("Python");
        myFixture.configureByText("hello.txt", "just sample");

        CourseManager manager = new CourseManager();
        Lesson lesson = manager.findLesson("Ruby Comment Line");
        manager.openLesson(myFixture.getProject(), lesson);
        System.err.println("Result:\n" + myFixture.getEditor().getDocument().getCharsSequence());
    }
}
