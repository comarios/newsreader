import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import android.test.suitebuilder.annotation.SmallTest;

import com.ucl.adaptationmechanism.RuleLoader;
import com.ucl.news.main.MainActivity;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by danyaalmasood on 22/03/2017.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class RuleFeaturesTest {

    private Context context;
    private RuleLoader rl;
    private List<RuleLoader.Rule> rules;

    @Before
    public void setup() throws Exception{
        context = InstrumentationRegistry.getTargetContext();
        rl = new RuleLoader(context);
        rules = rl.getRules();
    }

    @Test
    public void testTrackerFeatures() {

        List<Double> percentages = new ArrayList<>(Arrays.asList(100.0, 0.0, 0.0));
        List<String> featureList = new ArrayList<>(Arrays.asList("trackerLayout", "trackerTop", "paragraphSummary", "colourGradient", "originalStory", "pushNotifications"));

        Assert.assertEquals(featureList, MainActivity.matchRule(rules, percentages).getFeatureList());
    }

    @Test
    public void testReviewerFeatures() {

        List<Double> percentages = new ArrayList<>(Arrays.asList(0.0, 100.0, 0.0));
        List<String> featureList = new ArrayList<>(Arrays.asList("reviewerLayout", "highlightedTerms", "accordionInfo", "relatedArticles"));

        Assert.assertEquals(featureList, MainActivity.matchRule(rules, percentages).getFeatureList());
    }

    @Test
    public void testDipperFeatures() {

        List<Double> percentages = new ArrayList<>(Arrays.asList(0.0, 0.0, 100.0));
        List<String> featureList = new ArrayList<>(Arrays.asList("dipperLayout", "dipperTop", "wordcloud", "bulletPointSummary", "relatedArticles"));

        Assert.assertEquals(featureList, MainActivity.matchRule(rules, percentages).getFeatureList());
    }

    @Test
    public void testTrackerReviewerFeatures() {

        List<Double> percentages = new ArrayList<>(Arrays.asList(50.0, 50.0, 0.0));
        List<String> featureList = new ArrayList<>(Arrays.asList("trackerLayout", "trackerTop", "paragraphSummary", "colourGradient", "originalStory"));

        Assert.assertEquals(featureList, MainActivity.matchRule(rules, percentages).getFeatureList());
    }

    @Test
    public void testReviewerDipperFeatures() {

        List<Double> percentages = new ArrayList<>(Arrays.asList(0.0, 50.0, 50.0));
        List<String> featureList = new ArrayList<>(Arrays.asList("reviewerLayout", "highlightedTerms", "accordionInfo", "relatedArticles"));

        Assert.assertEquals(featureList, MainActivity.matchRule(rules, percentages).getFeatureList());
    }
}
