#!/usr/bin/env bash
# ==============================================================================
# Food Store Management System - Dynamic Date Data Seeder (Bash)
# Generates realistic sample CSV data relative to the current execution date.
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_DIR="$SCRIPT_DIR/data"
mkdir -p "$DATA_DIR"

echo "Seeding Food Store Management System sample data based on current date..."

# Compute relative ISO dates
if date -v-1d >/dev/null 2>&1; then
    # BSD date (macOS)
    d_m30=$(date -v-30d +"%Y-%m-%d")
    d_m25=$(date -v-25d +"%Y-%m-%d")
    d_m20=$(date -v-20d +"%Y-%m-%d")
    d_m15=$(date -v-15d +"%Y-%m-%d")
    d_m10=$(date -v-10d +"%Y-%m-%d")
    d_m5=$(date -v-5d +"%Y-%m-%d")
    d_m2=$(date -v-2d +"%Y-%m-%d")
    d_now=$(date +"%Y-%m-%d")
    d_p3=$(date -v+3d +"%Y-%m-%d")
    d_p15=$(date -v+15d +"%Y-%m-%d")
    d_p30=$(date -v+30d +"%Y-%m-%d")
    d_p60=$(date -v+60d +"%Y-%m-%d")
    d_p90=$(date -v+90d +"%Y-%m-%d")
    d_p180=$(date -v+180d +"%Y-%m-%d")

    t_today_m1=$(date +"%Y-%m-%dT09:15:00")
    t_today_m2=$(date +"%Y-%m-%dT11:30:00")
    t_today_m3=$(date +"%Y-%m-%dT14:45:00")
    t_d1_1=$(date -v-1d +"%Y-%m-%dT10:20:00")
    t_d1_2=$(date -v-1d +"%Y-%m-%dT16:10:00")
    t_d2_1=$(date -v-2d +"%Y-%m-%dT15:30:00")
    t_d3_1=$(date -v-3d +"%Y-%m-%dT08:45:00")
    t_d4_1=$(date -v-4d +"%Y-%m-%dT11:00:00")
    t_d5_1=$(date -v-5d +"%Y-%m-%dT17:15:00")
    t_d6_1=$(date -v-6d +"%Y-%m-%dT14:20:00")
    t_d7_1=$(date -v-7d +"%Y-%m-%dT10:30:00")
    t_d8_1=$(date -v-8d +"%Y-%m-%dT12:15:00")
    t_d10_1=$(date -v-10d +"%Y-%m-%dT16:45:00")
    t_d12_1=$(date -v-12d +"%Y-%m-%dT09:30:00")
    t_d15_1=$(date -v-15d +"%Y-%m-%dT15:00:00")
else
    # GNU date (Linux / Git Bash)
    d_m30=$(date -d "30 days ago" +"%Y-%m-%d")
    d_m25=$(date -d "25 days ago" +"%Y-%m-%d")
    d_m20=$(date -d "20 days ago" +"%Y-%m-%d")
    d_m15=$(date -d "15 days ago" +"%Y-%m-%d")
    d_m10=$(date -d "10 days ago" +"%Y-%m-%d")
    d_m5=$(date -d "5 days ago" +"%Y-%m-%d")
    d_m2=$(date -d "2 days ago" +"%Y-%m-%d")
    d_now=$(date +"%Y-%m-%d")
    d_p3=$(date -d "3 days" +"%Y-%m-%d")
    d_p15=$(date -d "15 days" +"%Y-%m-%d")
    d_p30=$(date -d "30 days" +"%Y-%m-%d")
    d_p60=$(date -d "60 days" +"%Y-%m-%d")
    d_p90=$(date -d "90 days" +"%Y-%m-%d")
    d_p180=$(date -d "180 days" +"%Y-%m-%d")

    t_today_m1=$(date +"%Y-%m-%dT09:15:00")
    t_today_m2=$(date +"%Y-%m-%dT11:30:00")
    t_today_m3=$(date +"%Y-%m-%dT14:45:00")
    t_d1_1=$(date -d "1 day ago" +"%Y-%m-%dT10:20:00")
    t_d1_2=$(date -d "1 day ago" +"%Y-%m-%dT16:10:00")
    t_d2_1=$(date -d "2 days ago" +"%Y-%m-%dT15:30:00")
    t_d3_1=$(date -d "3 days ago" +"%Y-%m-%dT08:45:00")
    t_d4_1=$(date -d "4 days ago" +"%Y-%m-%dT11:00:00")
    t_d5_1=$(date -d "5 days ago" +"%Y-%m-%dT17:15:00")
    t_d6_1=$(date -d "6 days ago" +"%Y-%m-%dT14:20:00")
    t_d7_1=$(date -d "7 days ago" +"%Y-%m-%dT10:30:00")
    t_d8_1=$(date -d "8 days ago" +"%Y-%m-%dT12:15:00")
    t_d10_1=$(date -d "10 days ago" +"%Y-%m-%dT16:45:00")
    t_d12_1=$(date -d "12 days ago" +"%Y-%m-%dT09:30:00")
    t_d15_1=$(date -d "15 days ago" +"%Y-%m-%dT15:00:00")
fi

cat <<EOF > "$DATA_DIR/products.csv"
id,name,category,unit,price,isDeleted
P00001,ST25 Fragrant Rice 5kg,Grains,Bag,165000.0,false
P00002,Hao Hao Sour Spicy Noodles,Noodles,Pack,4500.0,false
P00003,Australian Beef Sirloin 500g,Meat,Tray,145000.0,false
P00004,Norwegian Salmon Fillet 300g,Seafood,Tray,195000.0,true
P00005,Dalat Hydroponic Lettuce,Produce,Bag,22000.0,false
P00006,Vinamilk Fresh Milk 1L,Dairy,Carton,36000.0,false
P00007,CP Pork & Shrimp Dumplings,Frozen,Pack,68000.0,false
P00008,Three Lady Cooks Canned Tuna,Canned,Can,32000.0,true
P00009,La Vie Mineral Water 1.5L,Beverages,Bottle,24000.0,false
P00010,Nam Ngu Phu Quoc Fish Sauce,Condiments,Bottle,48000.0,false
P00011,Oishi Prawn Crackers 80g,Snacks,Pack,26000.0,false
P00012,ABC Whole Wheat Sandwich Bread,Bakery,Pack,25000.0,false
P00013,Discontinued French Butter,Dairy,Box,85000.0,true
P00014,Chinsu Chili Sauce 250g,Condiments,Bottle,18000.0,false
P00015,Dalat Fresh Strawberries 250g,Produce,Box,65000.0,false
P00016,Expired Seaweed Snacks,Snacks,Pack,15000.0,true
P00017,Heineken Beer Can 330ml,Beverages,Can,21000.0,false
P00018,Mezan Pure Soybean Oil 1L,Condiments,Bottle,52000.0,false
EOF

cat <<EOF > "$DATA_DIR/customers.csv"
id,fullName,phone,address,customerType,isDeleted
C00001,Nguyen Van An,0901234567,"12 Le Loi, Ben Nghe, Q1, Ho Chi Minh",VIP,false
C00002,Tran Thi Binh,0912345678,"45 Nguyen Hue, Q1, HCM",Regular,false
C00003,Le Hoang Minh,0987654321,"108 Dien Bien Phu, P6, Q3, TP.HCM",VIP,false
C00004,Pham Thu Ha,0938112233,"22 Phan Xich Long, Phu Nhuan, HCM",Regular,false
C00005,Vo Quoc Bao,0979445566,"88 Nguyen Van Linh, Tan Phong, Q7, Ho Chi Minh",VIP,false
C00006,Dang Minh Tuan,0909887766,"15 Cach Mang Thang 8, P10, Q3, HCM",Regular,false
C00007,Hoang Gia Huy,0932998877,"30 Ly Tu Trong, Q1, Ho Chi Minh",Regular,true
C00008,Tran Cao Thanh,0372240629,"Duong 1a, Linh Trung, Thu Duc, HCM",VIP,false
C00009,Nguyen Van A,0372240628,"2 Ba Trung, Ben Nghe, Q1, HCM",Regular,false
C00010,Nguyen Van B,0372240627,"12/3A Nguyen Trai, P2, Q5, TP.HCM",Regular,false
C00011,Do Thi Mai,0903123456,"55 Vo Van Tan, P6, Q3, Ho Chi Minh",VIP,false
C00012,Pham Thanh Thao,0944556677,"15 Le Duan, Ben Nghe, Q1, HCM",VIP,true
C00013,Bui Hoang Nam,0988776655,"102 Nguyen Thi Minh Khai, Q1, HCM",Regular,false
EOF

cat <<EOF > "$DATA_DIR/batches.csv"
batchId,productId,importDate,productionDate,expirationDate,quantity
B00001,P00001,$d_m5,$d_m20,$d_p180,45
B00002,P00001,$d_m2,$d_m15,$d_p180,15
B00003,P00002,$d_m10,$d_m25,$d_p90,100
B00004,P00002,$d_m5,$d_m15,$d_p90,40
B00005,P00003,$d_m5,$d_m10,$d_p15,4
B00006,P00005,$d_m2,$d_m5,$d_p3,12
B00007,P00006,$d_m10,$d_m15,$d_p30,20
B00008,P00006,$d_m5,$d_m10,$d_p30,12
B00009,P00007,$d_m15,$d_m30,$d_p90,18
B00010,P00009,$d_m10,$d_m25,$d_p180,48
B00011,P00010,$d_m20,$d_m30,$d_p180,22
B00012,P00011,$d_m10,$d_m20,$d_p90,40
B00013,P00012,$d_m5,$d_m10,$d_p3,3
B00014,P00014,$d_m15,$d_m25,$d_p180,30
B00015,P00015,$d_m2,$d_m5,$d_p3,8
B00016,P00017,$d_m10,$d_m20,$d_p180,60
B00017,P00018,$d_m15,$d_m25,$d_p180,25
B00018,P00006,$d_m30,$d_m30,$d_m2,5
B00019,P00012,$d_m25,$d_m30,$d_m2,2
EOF

cat <<EOF > "$DATA_DIR/orders.csv"
orderId,customerId,orderDate,totalAmount,discount,finalAmount
O00001,C00001,$t_d15_1,378000.0,37800.0,340200.0
O00002,C00002,$t_d12_1,117000.0,0.0,117000.0
O00003,C00003,$t_d10_1,485000.0,48500.0,436500.0
O00004,GUEST,$t_d8_1,124000.0,0.0,124000.0
O00005,C00001,$t_d7_1,351000.0,35100.0,315900.0
O00006,C00005,$t_d6_1,194000.0,19400.0,174600.0
O00007,C00006,$t_d5_1,72000.0,0.0,72000.0
O00008,C00008,$t_d4_1,330000.0,33000.0,297000.0
O00009,GUEST,$t_d3_1,90000.0,0.0,90000.0
O00010,C00011,$t_d2_1,233000.0,23300.0,209700.0
O00011,C00009,$t_d1_1,45000.0,0.0,45000.0
O00012,C00004,$t_d1_2,136000.0,0.0,136000.0
O00013,C00001,$t_today_m1,183000.0,18300.0,164700.0
O00014,GUEST,$t_today_m2,54000.0,0.0,54000.0
O00015,C00003,$t_today_m3,290000.0,29000.0,261000.0
EOF

cat <<EOF > "$DATA_DIR/order_details.csv"
orderId,productId,quantity,unitPrice
O00001,P00001,2,165000.0
O00001,P00010,1,48000.0
O00002,P00005,2,22000.0
O00002,P00006,2,36000.0
O00002,P00002,2,4500.0
O00003,P00003,2,145000.0
O00003,P00004,1,195000.0
O00004,P00009,3,24000.0
O00004,P00011,2,26000.0
O00005,P00001,1,165000.0
O00005,P00007,2,68000.0
O00005,P00012,2,25000.0
O00006,P00008,4,32000.0
O00006,P00015,1,65000.0
O00007,P00006,2,36000.0
O00008,P00001,2,165000.0
O00009,P00002,10,4500.0
O00009,P00011,1,26000.0
O00009,P00014,1,18000.0
O00010,P00003,1,145000.0
O00010,P00005,4,22000.0
O00011,P00002,10,4500.0
O00012,P00007,2,68000.0
O00013,P00001,1,165000.0
O00013,P00014,1,18000.0
O00014,P00011,1,26000.0
O00014,P00017,1,21000.0
O00014,P00002,1,7000.0
O00015,P00003,2,145000.0
EOF

echo "Data seeding completed successfully! Generated: 18 products, 13 customers, 19 batches, 15 orders."
