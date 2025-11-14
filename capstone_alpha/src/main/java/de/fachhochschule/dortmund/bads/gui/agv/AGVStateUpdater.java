package de.fachhochschule.dortmund.bads.gui.agv;

import javax.swing.SwingUtilities;

import de.fachhochschule.dortmund.bads.resources.AGV;

/**
 * Handles AGV state updates from backend
 */
public class AGVStateUpdater {
    
    private final AGV agv;
    private final StateUpdateCallback callback;
    
    public interface StateUpdateCallback {
        void onStateUpdate(int batteryLevel, AGV.AGVState state, boolean hasCargo, boolean isMoving);
    }
    
    public AGVStateUpdater(AGV agv, StateUpdateCallback callback) {
        this.agv = agv;
        this.callback = callback;
    }
    
    public void updateFromBackend() {
        if (agv != null && callback != null) {
            SwingUtilities.invokeLater(() -> {
                int batteryLevel = agv.getBatteryLevel();
                AGV.AGVState state = agv.getState();
                boolean hasCargo = (state == AGV.AGVState.BUSY);
                boolean isMoving = (state == AGV.AGVState.BUSY || state == AGV.AGVState.MOVING_TO_CHARGE);
                callback.onStateUpdate(batteryLevel, state, hasCargo, isMoving);
            });
        }
    }
    
    public boolean hasBackend() {
        return agv != null;
    }
    
    public AGV getAGV() {
        return agv;
    }
}
