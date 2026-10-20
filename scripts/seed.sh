#!/usr/bin/env bash
# scripts/seed.sh
# Generates dynamic, valid demo CSV datasets in data/ with dates relative to today.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_DIR="$SCRIPT_DIR/../data"

mkdir -p "$DATA_DIR"

# Compute dates portably (supporting GNU date and BSD date)
calc_date() {
    local days=$1
    if date -v+1d >/dev/null 2>&1; then
        # BSD date (macOS)
        if [ "$days" -ge 0 ]; then
            date -v+"${days}"d "+%Y-%m-%d"
        else
            date -v"${days}"d "+%Y-%m-%d"
        fi
    else
        # GNU date (Linux)
        date -d "$days days" "+%Y-%m-%d"
    fi
}

calc_datetime() {
    local days=$1
    if date -v+1d >/dev/null 2>&1; then
        # BSD date (macOS)
        if [ "$days" -ge 0 ]; then
            date -v+"${days}"d "+%Y-%m-%dT%H:%M:%S"
        else
            date -v"${days}"d "+%Y-%m-%dT%H:%M:%S"
        fi
    else
        # GNU date (Linux)
        date -d "$days days" "+%Y-%m-%dT%H:%M:%S"
    fi
}

D_PROD_PAST_30=$(calc_date -30)
D_PROD_PAST_15=$(calc_date -15)
D_PROD_PAST_5=$(calc_date -5)
D_PROD_PAST_2=$(calc_date -2)

D_IMPORT_PAST_20=$(calc_date -20)
D_IMPORT_PAST_10=$(calc_date -10)
D_IMPORT_PAST_3=$(calc_date -3)
D_IMPORT_PAST_1=$(calc_date -1)

D_EXP_ACTIVE_LONG=$(calc_date 180)
D_EXP_ACTIVE_MED=$(calc_date 60)
D_EXP_ACTIVE_SHORT=$(calc_date 14)
D_EXP_NEAR=$(calc_date 3)
D_EXP_EXPIRED=$(calc_date -4)

D_ORDER_TODAY=$(calc_datetime 0)
D_ORDER_YESTERDAY=$(calc_datetime -1)
D_ORDER_LAST_MONTH=$(calc_datetime -20)

cat <<EOF > "$DATA_DIR/products.csv"
id,name,category,unit,price,isDeleted
P00001,ST25 Fragrant Rice 5kg,Grains,Bag,165000.0,false
P00002,Hao Hao Sour Spicy Noodles,Noodles,Pack,4500.0,false
P00003,Australian Beef Sirloin 500g,Meat,Tray,145000.0,false
P00004,Norwegian Salmon Fillet 300g,Seafood,Tray,195000.0,false
P00005,Dalat Hydroponic Lettuce,Produce,Bag,22000.0,false
P00006,Vinamilk Fresh Milk 1L,Dairy,Carton,36000.0,false
P00007,CP Pork & Shrimp Dumplings,Frozen,Pack,68000.0,false
P00008,Three Lady Cooks Canned Tuna,Canned,Can,32000.0,false
P00009,La Vie Mineral Water 1.5L,Beverages,Bottle,24000.0,false
P00010,Nam Ngu Phu Quoc Fish Sauce,Condiments,Bottle,48000.0,false
P00011,Oishi Prawn Crackers 80g,Snacks,Pack,26000.0,false
P00012,ABC Whole Wheat Sandwich Bread,Bakery,Pack,25000.0,false
P00013,Discontinued Imported Butter,Dairy,Box,95000.0,true
EOF

cat <<EOF > "$DATA_DIR/batches.csv"
batchId,productId,importDate,productionDate,expirationDate,quantity
B00001,P00001,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,15
B00002,P00001,$D_IMPORT_PAST_10,$D_PROD_PAST_15,$D_EXP_ACTIVE_LONG,30
B00003,P00002,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,80
B00004,P00002,$D_IMPORT_PAST_10,$D_PROD_PAST_15,$D_EXP_ACTIVE_LONG,60
B00005,P00003,$D_IMPORT_PAST_3,$D_PROD_PAST_5,$D_EXP_NEAR,4
B00006,P00003,$D_IMPORT_PAST_10,$D_PROD_PAST_15,$D_EXP_EXPIRED,3
B00007,P00004,$D_IMPORT_PAST_3,$D_PROD_PAST_5,$D_EXP_ACTIVE_SHORT,5
B00008,P00005,$D_IMPORT_PAST_1,$D_PROD_PAST_2,$D_EXP_NEAR,12
B00009,P00006,$D_IMPORT_PAST_10,$D_PROD_PAST_15,$D_EXP_ACTIVE_SHORT,8
B00010,P00006,$D_IMPORT_PAST_3,$D_PROD_PAST_5,$D_EXP_ACTIVE_MED,24
B00011,P00007,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,18
B00012,P00008,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,35
B00013,P00009,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,48
B00014,P00010,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,22
B00015,P00011,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,40
B00016,P00012,$D_IMPORT_PAST_10,$D_PROD_PAST_15,$D_EXP_EXPIRED,6
B00017,P00012,$D_IMPORT_PAST_1,$D_PROD_PAST_2,$D_EXP_NEAR,3
EOF

cat <<EOF > "$DATA_DIR/customers.csv"
id,fullName,phone,address,customerType,isDeleted
C00001,Nguyễn Văn An,0901234567,"12 Lê Lợi, Ben Nghe Ward, District 1, Ho Chi Minh City",VIP,false
C00002,Trần Thị Bình,0912345678,"45 Nguyễn Huệ, District 1, Ho Chi Minh City",Regular,false
C00003,Lê Hoàng Minh,0987654321,"108 Điện Biên Phủ, Ward 6, District 3, Ho Chi Minh City",VIP,false
C00004,Phạm Thu Hà,0938112233,"22 Phan Xích Long, Phu Nhuan District, Ho Chi Minh City",Regular,false
C00005,Võ Quốc Bảo,0979445566,"88 Nguyễn Văn Linh, District 7, Ho Chi Minh City",VIP,false
C00006,Đặng Minh Tuấn,0909887766,"15 Cách Mạng Tháng 8, District 10, Ho Chi Minh City",Regular,false
C00007,Hoàng Gia Huy,0932998877,"30 Lý Tự Trọng, District 1, Ho Chi Minh City",Regular,true
EOF

cat <<EOF > "$DATA_DIR/orders.csv"
orderId,customerId,orderDate,totalAmount,discount,finalAmount
O00001,C00001,$D_ORDER_LAST_MONTH,378000.0,37800.0,340200.0
O00002,C00002,$D_ORDER_YESTERDAY,117000.0,0.0,117000.0
O00003,C00003,$D_ORDER_YESTERDAY,485000.0,48500.0,436500.0
O00004,GUEST,$D_ORDER_TODAY,124000.0,0.0,124000.0
O00005,C00001,$D_ORDER_TODAY,351000.0,35100.0,315900.0
O00006,C00005,$D_ORDER_TODAY,194000.0,19400.0,174600.0
EOF

cat <<EOF > "$DATA_DIR/order_details.csv"
orderId,productId,quantity,unitPrice
O00001,P00001,2,165000.0
O00001,P00010,1,48000.0
O00002,P00002,10,4500.0
O00002,P00006,2,36000.0
O00003,P00003,2,145000.0
O00003,P00004,1,195000.0
O00004,P00009,3,24000.0
O00004,P00011,2,26000.0
O00005,P00001,1,165000.0
O00005,P00007,2,68000.0
O00005,P00012,2,25000.0
O00006,P00008,4,32000.0
O00006,P00005,3,22000.0
EOF

echo " [OK] Seed data successfully generated in $DATA_DIR"
