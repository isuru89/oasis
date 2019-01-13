package io.github.isuru.oasis.services.dto.stats;

import java.util.List;

/**
 * @author iweerarathna
 */
public class BadgeBreakdownResDto {

    private long count;
    private List<BadgeRecordDto> records;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<BadgeRecordDto> getRecords() {
        return records;
    }

    public void setRecords(List<BadgeRecordDto> records) {
        this.records = records;
    }
}
