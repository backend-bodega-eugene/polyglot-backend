using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Prototype
{
    internal class Eugene: IEugene
    {
        public string Laohu { get; set; } = "eugene";
        public int Age { get; set; } = 18;
        public int BreathsPerMinute { get; set; } = 20;
        public IEugene Clone()
        {
            Eugene eugene = new Eugene();
            eugene.Laohu = this.Laohu;
            eugene.Age = this.Age;
            eugene.BreathsPerMinute = this.BreathsPerMinute;
            return (IEugene)eugene;
        }
        public IEugene EugeneMemberwiseClone()
        {
            return (IEugene)this.MemberwiseClone();
        }
    }
}
