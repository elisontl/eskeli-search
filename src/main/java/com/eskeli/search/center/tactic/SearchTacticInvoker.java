package com.eskeli.search.center.tactic;

import com.eskeli.search.center.tactic.specific.SampleSearchTactic;

/**
 * Class Desc : 策略调用
 *
 * @author elisontl
 */
public class SearchTacticInvoker {

    /**
     * Search tactic property obj .
     */
    public BasicSearchTactic searchTactic;

    /**
     * Structure injection search tactic .
     * @param searchTactic : BasicSearchTactic
     */
    public SearchTacticInvoker(BasicSearchTactic searchTactic) {
        this.searchTactic = searchTactic;
    }

    /**
     * No args structure, injection sample search tactic .
     */
    public SearchTacticInvoker() {
        if (searchTactic == null) {
            searchTactic = new SampleSearchTactic();
        }
    }

}
