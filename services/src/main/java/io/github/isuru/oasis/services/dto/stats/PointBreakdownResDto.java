package io.github.isuru.oasis.services.dto.stats;

import java.util.List;

/**
 * @author iweerarathna
 */
public class PointBreakdownResDto {

    private long count;
    private List<PointRecordDto> records;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<PointRecordDto> getRecords() {
        return records;
    }

    public void setRecords(List<PointRecordDto> records) {
        this.records = records;
    }
}
