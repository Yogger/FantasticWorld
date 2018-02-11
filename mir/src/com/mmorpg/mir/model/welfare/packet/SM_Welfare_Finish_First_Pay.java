package com.mmorpg.mir.model.welfare.packet;

public class SM_Welfare_Finish_First_Pay {
	/** 是否完成首充 */
	private boolean finishFirstPay;
	/** 是否已领取首充奖励 */
	private boolean drawVipfirstPayReward;

	public static SM_Welfare_Finish_First_Pay valueOf(boolean finishFirstPay, boolean drawVipfirstPayReward) {
		SM_Welfare_Finish_First_Pay result = new SM_Welfare_Finish_First_Pay();
		result.finishFirstPay = finishFirstPay;
		result.drawVipfirstPayReward = drawVipfirstPayReward;
		return result;

	}

	public boolean isFinishFirstPay() {
		return finishFirstPay;
	}

	public void setFinishFirstPay(boolean finishFirstPay) {
		this.finishFirstPay = finishFirstPay;
	}

	public boolean isDrawVipfirstPayReward() {
		return drawVipfirstPayReward;
	}

	public void setDrawVipfirstPayReward(boolean drawVipfirstPayReward) {
		this.drawVipfirstPayReward = drawVipfirstPayReward;
	}

}