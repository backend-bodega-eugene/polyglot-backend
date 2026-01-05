fn main() {
    //打印10行10列的星星
    // let star="⭐";
    // let mut i=0;
    //空心正方形
    // while i<10 {
    //     let mut j=0;
    //     while j<10 {
    //         if j==0 || j==9 || i==0 || i==9 {
    //         print!("{}",star);      
    //         }
    //         else {
    //               print!("  ");  
    //         }
    //           j+=1;
    //     }
    //     println!();
    //     i+=1;
    // }
    //三角形
    // let star="⭐";
    // let mut i=0;
    // let height=10;
    // while i<10{
     
    //     let mut s=0;
    //     while s <height-1-i {
    //         print!("  ");  
    //         s+=1;
    //     }

    //     let mut j=0;
    //     while j<2 * i + 1{
    //         print!("{}",star);   
    //         j+=1;
    //     }
    //     println!();
    //     i+=1;
    // }
    //倒过来的星星
    let star = "⭐";
    let height = 10;

    let mut i = 0;
    while i < height {
        
        // 空格
        let mut s = 0;
        while s < i {
            print!("  ");
            s += 1;
        }

        // 星星
        let mut j = 0;
        while j < 2 * (height - i) - 1 {
            print!("{}", star);
            j += 1;
        }

        println!();
        i += 1;
    }

    
}
