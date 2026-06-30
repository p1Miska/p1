package dev.evvie.waylandcraft.item;

import dev.evvie.waylandcraft.WaylandCraft;
import dev.evvie.waylandcraft.bridge.WLCToplevel;

/**
 * Purely client-side "phantom" replacement for WindowItemManager.
 *
 * No network packets are ever sent, no ItemStack is ever placed into the
 * real (server-authoritative) player inventory. Instead, this just
 * remembers up to 9 toplevel handles chosen by the player and lets you
 * trigger the same grab/use behaviour as a real window item via
 * keybinds, with icons drawn by WaylandHudRenderer on top of the hotbar.
 *
 * Because the server is never informed, this works identically on any
 * server regardless of whether it has waylandcraft installed.
 */
public class PhantomWindowSlots {

	public static final int SLOT_COUNT = 9;

	// handle = 0 means empty slot
	private final long[] handles = new long[SLOT_COUNT];

	private final WaylandCraft wlc;

	public PhantomWindowSlots(WaylandCraft wlc) {
		this.wlc = wlc;
	}

	public void set(int slot, WLCToplevel toplevel) {
		if(slot < 0 || slot >= SLOT_COUNT) return;
		handles[slot] = toplevel == null ? 0 : toplevel.getHandle();
	}

	public void clear(int slot) {
		if(slot < 0 || slot >= SLOT_COUNT) return;
		handles[slot] = 0;
	}

	public boolean isEmpty(int slot) {
		if(slot < 0 || slot >= SLOT_COUNT) return true;
		return handles[slot] == 0;
	}

	// Resolves the slot back to a live toplevel, clearing the slot if the
	// window has been closed in the meantime.
	public WLCToplevel get(int slot) {
		if(slot < 0 || slot >= SLOT_COUNT) return null;
		long handle = handles[slot];
		if(handle == 0) return null;
		if(wlc.bridge == null) return null;

		for(WLCToplevel toplevel : wlc.bridge.getMappedToplevels()) {
			if(toplevel.getHandle() == handle) return toplevel;
		}

		// window is gone, forget it
		handles[slot] = 0;
		return null;
	}

	// Call every tick while the slot's keybind is held down.
	// Mirrors what WindowItem.onUseTick() -> WaylandCraft.startUsingWindowItem()
	// would have done with a real item, but driven directly off the handle.
	public void useTick(int slot) {
		WLCToplevel toplevel = get(slot);
		if(toplevel == null) return;

		wlc.startUsingPhantomWindow(toplevel);
	}

}
