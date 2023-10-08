package net.runelite.client.plugins.nateplugins.moneymaking.natepieshells;

import net.runelite.client.plugins.envisionplugins.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Inventory;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;

public class PieScript extends Script {

    public static double version = 1.2;
    public static int totalPieShellsMade = 0;

    public boolean run(PieConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;

            try {
                if (Microbot.pauseAllScripts) return;
                if (Inventory.getAmountForItem("pie dish") > 0 && (Inventory.getAmountForItem("pastry dough") > 0)) {
                    Inventory.useItemOnItem("pie dish", "pastry dough");
                    sleepUntilOnClientThread(() -> Rs2Widget.getWidget(17694734) != null);
                    keyPress('1');
                    sleepUntilOnClientThread(() -> !Inventory.hasItem("pie dish"),25000);

                    totalPieShellsMade += 14;   // rough example, but you get the point
                    return;
                } else {
                    /**
                     *  Break handler logic:
                     *      First check to see if the Run Time Timer has finished running
                     *      Then notify that a break can be started
                     *      Finally lets wait until the break is over.
                     */
                    if (BreakHandlerScript.getHasRunTimeTimerFinished()) {
                        BreakHandlerScript.setSkillExperienceGained(new String[]{"NONE"});
                        BreakHandlerScript.setResourcesGained(new String[]{
                                "Pie Shells: " + totalPieShellsMade
                        });
                        BreakHandlerScript.setGpGained("WIP");

                        BreakHandlerScript.setLetBreakHandlerStartBreak(true);
                        sleepUntil(BreakHandlerScript::getIsBreakOver);
                        BreakHandlerScript.setLetBreakHandlerStartBreak(false);
                    }

                    bank();
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private void bank(){
        Rs2Bank.openBank();
        if(Rs2Bank.isOpen()){
            Rs2Bank.depositAll();
            if(Rs2Bank.hasItem("pie dish") &&  Rs2Bank.hasItem("pastry dough")) {
                Rs2Bank.withdrawItemX(true, "pie dish", 14);
                sleepUntilOnClientThread(() -> Inventory.hasItem("pie dish"));
                Rs2Bank.withdrawItemX(true, "pastry dough", 14);
                sleepUntilOnClientThread(() -> Inventory.hasItem("pastry dough"));
            } else {
                Microbot.getNotifier().notify("Run out of Materials");
                shutdown();
            }
        }
        Rs2Bank.closeBank();
        sleepUntilOnClientThread(() -> !Rs2Bank.isOpen());
    }
}
