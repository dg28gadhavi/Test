package com.sec.internal.ims.servicemodules.ss;

import java.util.ArrayList;
import java.util.List;

abstract class SsRuleData implements Cloneable {
    protected boolean active;
    protected List<SsRule> rules = new ArrayList();

    /* access modifiers changed from: protected */
    public abstract SsRuleData clone();

    /* access modifiers changed from: package-private */
    public abstract void copyRule(SsRule ssRule);

    /* access modifiers changed from: package-private */
    public abstract SsRule getRule(int i, MEDIA media);

    static class SsRule {
        Condition conditions = new Condition();
        String ruleId;

        SsRule() {
        }
    }

    SsRuleData() {
    }

    /* access modifiers changed from: package-private */
    public SsRule findRule(int i, MEDIA media) {
        for (SsRule next : this.rules) {
            Condition condition = next.conditions;
            if (condition.condition == i && condition.media.contains(media)) {
                return next;
            }
        }
        return null;
    }

    static void makeInternalRule(SsRule ssRule, int i, MEDIA media) {
        Condition condition = ssRule.conditions;
        condition.condition = i;
        condition.state = false;
        condition.action = 0;
        condition.media = new ArrayList();
        ssRule.conditions.media.add(media);
    }

    /* access modifiers changed from: package-private */
    public void setRule(SsRule ssRule) {
        for (SsRule next : this.rules) {
            Condition condition = next.conditions;
            int i = condition.condition;
            Condition condition2 = ssRule.conditions;
            if (i == condition2.condition && condition.media.equals(condition2.media)) {
                this.rules.remove(next);
                this.rules.add(ssRule);
                return;
            }
        }
        this.rules.add(ssRule);
    }

    /* access modifiers changed from: package-private */
    public void replaceRule(SsRule ssRule) {
        for (SsRule next : this.rules) {
            Condition condition = next.conditions;
            int i = condition.condition;
            Condition condition2 = ssRule.conditions;
            if (i == condition2.condition && condition.media.equals(condition2.media)) {
                List<SsRule> list = this.rules;
                list.set(list.indexOf(next), ssRule);
                return;
            }
        }
        this.rules.add(ssRule);
    }

    /* access modifiers changed from: package-private */
    public boolean isExist(int i, MEDIA media) {
        for (SsRule ssRule : this.rules) {
            Condition condition = ssRule.conditions;
            if (condition.condition == i && condition.media.contains(media)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isExist(int i) {
        for (SsRule ssRule : this.rules) {
            if (ssRule.conditions.condition == i) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void copySsRule(SsRule ssRule, SsRule ssRule2) {
        ssRule2.ruleId = ssRule.ruleId;
        Condition condition = new Condition();
        ssRule2.conditions = condition;
        Condition condition2 = ssRule.conditions;
        condition.condition = condition2.condition;
        condition.state = condition2.state;
        condition.action = condition2.action;
        condition.media = new ArrayList();
        ssRule2.conditions.media.addAll(ssRule.conditions.media);
        setRule(ssRule2);
    }

    /* access modifiers changed from: package-private */
    public void cloneSsDataInternal(SsRuleData ssRuleData) {
        ssRuleData.active = this.active;
        for (SsRule copyRule : this.rules) {
            ssRuleData.copyRule(copyRule);
        }
    }
}
