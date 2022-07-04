package njoyshadow.moreterminal.client.gui.layout;

import appeng.client.Point;
import appeng.menu.implementations.IOBusMenu;

public enum MTSlotGridLayout {

    /**
     * This layout is used for the peculiar grid of slots used in {@link IOBusMenu}.
     */
    IO_BUS_CONFIG {

        /**
         * Slots are laid out around the center slot 0. Slots 1-4 in the cardinal directions. Slots 5-8 in the diagonal
         * directions.
         */
        private final Point[] OFFSETS = new Point[] {
                new Point(0, 0),
                new Point(-18, 0),
                new Point(18, 0),
                new Point(0, -18),
                new Point(0, 18),
                new Point(-18, -18),
                new Point(18, -18),
                new Point(-18, 18),
                new Point(18, 18)
        };

        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return OFFSETS[Math.max(semanticIdx, 0) % OFFSETS.length].move(x, y);
        }

    },

    BREAK_AFTER_9COLS {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return MTSlotGridLayout.getRowBreakPosition(x, y, semanticIdx, 9);
        }
    },
    BREAK_AFTER_7COLS {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return MTSlotGridLayout.getRowBreakPosition(x, y, semanticIdx, 7);
        }
    },
    BREAK_AFTER_5COLS {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return MTSlotGridLayout.getRowBreakPosition(x, y, semanticIdx, 5);
        }
    },
    BREAK_AFTER_3COLS {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return MTSlotGridLayout.getRowBreakPosition(x, y, semanticIdx, 3);
        }
    },

    BREAK_AFTER_2COLS {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return MTSlotGridLayout.getRowBreakPosition(x, y, semanticIdx, 2);
        }
    },

    HORIZONTAL {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return new Point(x, y).move(semanticIdx * 18, 0);
        }
    },

    VERTICAL {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return new Point(x, y).move(0, semanticIdx * 18);
        }
    };

    private static Point getRowBreakPosition(int x, int y, int semanticIdx, int cols) {
        int row = semanticIdx / cols;
        int col = semanticIdx % cols;
        return new Point(x, y).move(col * 18, row * 18);
    }

    public abstract Point getPosition(int x, int y, int semanticIdx);

}
