package io.github.isuru.oasis.services.services.injector.consumers;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.MilestoneStateModel;
import io.github.isuru.oasis.services.services.injector.ConsumerContext;
import io.github.isuru.oasis.services.services.injector.MsgAcknowledger;
import io.github.isuru.oasis.services.utils.BufferedRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneStateConsumer extends BaseConsumer<MilestoneStateModel> {

    private static final Logger LOG = LoggerFactory.getLogger(MilestoneStateConsumer.class);

    private static final String GAME_UPDATE_MILESTONE_STATE_LOSS = "game/updateMilestoneStateLoss";
    private static final String GAME_UPDATE_MILESTONE_STATE = "game/updateMilestoneState";

    private final BufferedRecords stateBuffer;
    private final BufferedRecords lossStateBuffer;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param dao database access object
     */
    public MilestoneStateConsumer(IOasisDao dao, ConsumerContext contextInfo, MsgAcknowledger acknowledger) {
        super(dao, MilestoneStateModel.class, contextInfo, false, acknowledger);

        stateBuffer = new BufferedRecords(this::flushStates);
        lossStateBuffer = new BufferedRecords(this::flushLossStates);
        stateBuffer.init(contextInfo.getPool());
        lossStateBuffer.init(contextInfo.getPool());
    }

    @Override
    public void handleMessage(byte[] body, Object deliveryTag) {
        try {
            MilestoneStateModel message = MAPPER.readValue(body, MilestoneStateModel.class);
            handleModel(message, (long)deliveryTag);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> handle(MilestoneStateModel msg) {
        throw new IllegalStateException("This method should not be called at all!");
    }

    @Override
    void flushNow() {
        super.flushNow();

        stateBuffer.flushNow();
        lossStateBuffer.flushNow();
    }

    private void handleModel(MilestoneStateModel msg, long deliveryTag) {
        if (msg.getLossUpdate() != null && msg.getLossUpdate()) {
            Map<String, Object> map = ConsumerUtils.toMilestoneLossStateDaoData(msg);
            lossStateBuffer.push(new BufferedRecords.ElementRecord(map, deliveryTag));
        } else {
            Map<String, Object> map = ConsumerUtils.toMilestoneStateDaoData(msg);
            stateBuffer.push(new BufferedRecords.ElementRecord(map, deliveryTag));
        }
    }

    private void flushLossStates(List<BufferedRecords.ElementRecord> recordList) {
        flushRecords(recordList, GAME_UPDATE_MILESTONE_STATE_LOSS);
    }

    private void flushStates(List<BufferedRecords.ElementRecord> recordList) {
        flushRecords(recordList, GAME_UPDATE_MILESTONE_STATE);
    }

    @Override
    public void close() {
        super.close();
        stateBuffer.close();
        lossStateBuffer.close();
    }

    @Override
    public String getInsertScriptName() {
        throw new IllegalStateException("This method should not be called at all!");
    }
}
