﻿namespace CoreAutomata
{
    public class Region
    {
        public int X { get; set; }
        public int Y { get; set; }
        public int W { get; set; }
        public int H { get; set; }

        public int R => X + W;

        public int B => Y + H;

        public Location Location => new Location(X, Y);

        public Region(int X, int Y, int W, int H)
        {
            this.X = X;
            this.Y = Y;
            this.W = W;
            this.H = H;
        }

        public bool Contains(Region Region)
        {
            return X <= Region.X
                   && Y <= Region.Y
                   && R >= Region.R
                   && B >= Region.B;
        }

        public void Click()
        {
            var center = new Location(X + W / 2, Y + H / 2);

            center.Click();
        }

        public static Region operator *(Region Region, double Scale)
        {
            return new Region(
                (Region.X * Scale).Round(),
                (Region.Y * Scale).Round(),
                (Region.W * Scale).Round(),
                (Region.H * Scale).Round());
        }

        public override string ToString() => $"({X}, {Y}) {W}x{H}";
    }
}