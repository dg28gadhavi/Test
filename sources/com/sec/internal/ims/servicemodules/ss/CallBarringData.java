package com.sec.internal.ims.servicemodules.ss;

import com.sec.internal.ims.servicemodules.ss.SsRuleData;
import java.util.ArrayList;
import java.util.List;

public class CallBarringData extends SsRuleData {

    static class Rule extends SsRuleData.SsRule {
        List<ActionElm> actions = new ArrayList();
        boolean allow;
        List<String> target = new ArrayList();

        Rule() {
        }
    }

    /* access modifiers changed from: package-private */
    public Rule getRule(int i, MEDIA media) {
        Rule rule = (Rule) findRule(i, media);
        if (rule != null) {
            return rule;
        }
        return makeRule(i, media);
    }

    static Rule makeRule(int i, MEDIA media) {
        Rule rule = new Rule();
        SsRuleData.makeInternalRule(rule, i, media);
        rule.allow = false;
        return rule;
    }

    /* access modifiers changed from: package-private */
    public void copyRule(SsRuleData.SsRule ssRule) {
        Rule rule = (Rule) ssRule;
        Rule rule2 = new Rule();
        rule2.allow = rule.allow;
        rule2.target.addAll(rule.target);
        rule2.actions.addAll(rule.actions);
        super.copySsRule(rule, rule2);
    }

    public final CallBarringData clone() {
        CallBarringData callBarringData = new CallBarringData();
        cloneSsDataInternal(callBarringData);
        return callBarringData;
    }
}
