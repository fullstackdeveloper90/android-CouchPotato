package com.thorangs.couchpotato.ui.records

import com.thorangs.couchpotato.database.StepsLog

/**
 * Created by Laxman Bhattarai on 11/18/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
interface RecordsContract {
    interface View {
        fun showData(dataList: List<StepsLog>)
    }

    interface Presenter {
        fun retrieveData()
        fun finalize()
    }
}