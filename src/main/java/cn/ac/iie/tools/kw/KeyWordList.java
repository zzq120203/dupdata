package cn.ac.iie.tools.kw;

import java.util.ArrayList;
import java.util.List;

public class KeyWordList {

    private List<Long> rulesList;
    private List<Long> topicsList;
    private List<Long> themesList;

    private boolean isTarget;

    public KeyWordList() {
        rulesList = new ArrayList<>();
        topicsList = new ArrayList<>();
        themesList = new ArrayList<>();
        isTarget = false;
    }

    public boolean isTarget() {
        return isTarget;
    }

    public void setTarget(boolean target) {
        isTarget = target;
    }

    public List<Long> getRulesList() {
        return rulesList;
    }

    public void setRulesList(List<Long> rulesList) {
        this.rulesList = rulesList;
    }

    public List<Long> getTopicsList() {
        return topicsList;
    }

    public void setTopicsList(List<Long> topicsList) {
        this.topicsList = topicsList;
    }

    public List<Long> getThemesList() {
        return themesList;
    }

    public void setThemesList(List<Long> themesList) {
        this.themesList = themesList;
    }

    public void addRulesList(List<Long> rulesList) {
        this.rulesList.addAll(rulesList);
    }

    public void addTopicsList(List<Long> topicsList) {
        this.topicsList.addAll(topicsList);
    }

    public void addThemesList(List<Long> themesList) {
        this.themesList.addAll(themesList);
    }
}
