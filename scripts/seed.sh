set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_DIR="$SCRIPT_DIR/../data"

mkdir -p "$DATA_DIR"

calc_date() {
    local days=$1
    if date -v+1d >/dev/null 2>&1; then
        if [ "$days" -ge 0 ]; then
            date -v+"${days}"d "+%Y-%m-%d"
        else
            date -v"${days}"d "+%Y-%m-%d"
        fi
    else
        date -d "$days days" "+%Y-%m-%d"
    fi
}

calc_datetime() {
    local days=$1
    local time_str=$2
    local d_str
    if date -v+1d >/dev/null 2>&1; then
        if [ "$days" -ge 0 ]; then
            d_str=$(date -v+"${days}"d "+%Y-%m-%d")
        else
            d_str=$(date -v"${days}"d "+%Y-%m-%d")
        fi
    else
        d_str=$(date -d "$days days" "+%Y-%m-%d")
    fi
    echo "${d_str}T${time_str}"
}

D_PROD_PAST_60=$(calc_date -60)
D_PROD_PAST_30=$(calc_date -30)
D_PROD_PAST_15=$(calc_date -15)
D_PROD_PAST_5=$(calc_date -5)
D_PROD_PAST_2=$(calc_date -2)

D_IMPORT_PAST_45=$(calc_date -45)
D_IMPORT_PAST_20=$(calc_date -20)
D_IMPORT_PAST_10=$(calc_date -10)
D_IMPORT_PAST_3=$(calc_date -3)
D_IMPORT_PAST_1=$(calc_date -1)

D_EXP_ACTIVE_LONG=$(calc_date 180)
D_EXP_ACTIVE_MED=$(calc_date 60)
D_EXP_ACTIVE_SHORT=$(calc_date 14)
D_EXP_NEAR1=$(calc_date 2)
D_EXP_NEAR2=$(calc_date 5)
D_EXP_EXPIRED1=$(calc_date -3)
D_EXP_EXPIRED2=$(calc_date -12)

D_ORDER_PAST_25=$(calc_datetime -25 "10:30:00")
D_ORDER_PAST_20=$(calc_datetime -20 "14:15:00")
D_ORDER_YESTERDAY_MORN=$(calc_datetime -1 "09:15:00")
D_ORDER_YESTERDAY_EVE=$(calc_datetime -1 "17:45:00")
D_ORDER_TODAY_MORN=$(calc_datetime 0 "08:30:00")
D_ORDER_TODAY_NOON=$(calc_datetime 0 "12:15:00")
D_ORDER_TODAY_AFT=$(calc_datetime 0 "15:20:00")

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
P00013,Dutch Lady Condensed Milk,Dairy,Can,28000.0,false
P00014,Chinsu Chili Sauce 250g,Condiments,Bottle,18000.0,false
P00015,Discontinued Imported Butter,Dairy,Box,95000.0,true
P00016,Seasonal Lychee Fruit 1kg,Produce,Bag,55000.0,true
EOF

cat <<EOF > "$DATA_DIR/batches.csv"
batchId,productId,importDate,productionDate,expirationDate,quantity
B00001,P00001,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,15
B00002,P00001,$D_IMPORT_PAST_10,$D_PROD_PAST_15,$D_EXP_ACTIVE_LONG,30
B00003,P00002,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,80
B00004,P00002,$D_IMPORT_PAST_10,$D_PROD_PAST_15,$D_EXP_ACTIVE_LONG,60
B00005,P00003,$D_IMPORT_PAST_3,$D_PROD_PAST_5,$D_EXP_NEAR1,4
B00006,P00003,$D_IMPORT_PAST_10,$D_PROD_PAST_15,$D_EXP_EXPIRED1,3
B00007,P00004,$D_IMPORT_PAST_3,$D_PROD_PAST_5,$D_EXP_ACTIVE_SHORT,5
B00008,P00005,$D_IMPORT_PAST_1,$D_PROD_PAST_2,$D_EXP_NEAR2,12
B00009,P00006,$D_IMPORT_PAST_10,$D_PROD_PAST_15,$D_EXP_ACTIVE_SHORT,8
B00010,P00006,$D_IMPORT_PAST_3,$D_PROD_PAST_5,$D_EXP_ACTIVE_MED,24
B00011,P00007,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,18
B00012,P00008,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,35
B00013,P00009,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,48
B00014,P00010,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,22
B00015,P00011,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,40
B00016,P00012,$D_IMPORT_PAST_10,$D_PROD_PAST_15,$D_EXP_EXPIRED1,6
B00017,P00012,$D_IMPORT_PAST_1,$D_PROD_PAST_2,$D_EXP_NEAR1,3
B00018,P00013,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_ACTIVE_LONG,50
B00019,P00014,$D_IMPORT_PAST_45,$D_PROD_PAST_60,$D_EXP_ACTIVE_MED,0
B00020,P00014,$D_IMPORT_PAST_3,$D_PROD_PAST_5,$D_EXP_ACTIVE_LONG,4
B00021,P00015,$D_IMPORT_PAST_45,$D_PROD_PAST_60,$D_EXP_EXPIRED2,2
B00022,P00016,$D_IMPORT_PAST_20,$D_PROD_PAST_30,$D_EXP_EXPIRED2,10
B00023,P00016,$D_IMPORT_PAST_10,$D_PROD_PAST_15,$D_EXP_EXPIRED1,5
EOF

cat <<EOF > "$DATA_DIR/customers.csv"
id,fullName,phone,address,customerType,isDeleted
C00001,Nguyen Van An,0901234567,"12 Le Loi, Ben Nghe Ward, District 1, Ho Chi Minh City",VIP,false
C00002,Tran Thi Binh,0912345678,"45 Nguyen Hue, District 1, Ho Chi Minh City",Regular,false
C00003,Le Hoang Minh,0987654321,"108 Dien Bien Phu, Ward 6, District 3, Ho Chi Minh City",VIP,false
C00004,Pham Thu Ha,0938112233,"22 Phan Xich Long, Phu Nhuan District, Ho Chi Minh City",Regular,false
C00005,Vo Quoc Bao,0979445566,"88 Nguyen Van Linh, District 7, Ho Chi Minh City",VIP,false
C00006,Dang Minh Tuan,0909887766,"15 Cach Mang Thang 8, District 10, Ho Chi Minh City",Regular,false
C00007,Hoang Gia Huy,0932998877,"30 Ly Tu Trong, District 1, Ho Chi Minh City",Regular,true
C00008,Do Thi Mai,0945667788,"56 Hai Ba Trung, District 1, Ho Chi Minh City",VIP,false
C00009,Bui Quang Khai,0968123456,"72 Vo Thi Sau, District 3, Ho Chi Minh City",Regular,false
C00010,Trinh Hong Ngoc,0918776655,"19 Tran Hung Dao, District 5, Ho Chi Minh City",VIP,true
EOF

cat <<EOF > "$DATA_DIR/orders.csv"
orderId,customerId,orderDate,totalAmount,discount,finalAmount
O00001,C00001,$D_ORDER_PAST_25,378000.0,37800.0,340200.0
O00002,C00002,$D_ORDER_PAST_20,117000.0,0.0,117000.0
O00003,C00007,$D_ORDER_PAST_20,254000.0,0.0,254000.0
O00004,C00003,$D_ORDER_YESTERDAY_MORN,485000.0,48500.0,436500.0
O00005,C00004,$D_ORDER_YESTERDAY_EVE,172000.0,0.0,172000.0
O00006,GUEST,$D_ORDER_TODAY_MORN,124000.0,0.0,124000.0
O00007,C00001,$D_ORDER_TODAY_MORN,351000.0,35100.0,315900.0
O00008,C00005,$D_ORDER_TODAY_NOON,194000.0,19400.0,174600.0
O00009,C00008,$D_ORDER_TODAY_NOON,764000.0,76400.0,687600.0
O00010,C00001,$D_ORDER_TODAY_AFT,303000.0,30300.0,272700.0
EOF

cat <<EOF > "$DATA_DIR/order_details.csv"
orderId,productId,quantity,unitPrice
O00001,P00001,2,165000.0
O00001,P00010,1,48000.0
O00002,P00002,10,4500.0
O00002,P00006,2,36000.0
O00003,P00015,2,95000.0
O00003,P00008,2,32000.0
O00004,P00003,2,145000.0
O00004,P00004,1,195000.0
O00005,P00007,2,68000.0
O00005,P00014,2,18000.0
O00006,P00009,3,24000.0
O00006,P00011,2,26000.0
O00007,P00001,1,165000.0
O00007,P00007,2,68000.0
O00007,P00012,2,25000.0
O00008,P00008,4,32000.0
O00008,P00005,3,22000.0
O00009,P00003,2,145000.0
O00009,P00004,2,195000.0
O00009,P00013,3,28000.0
O00010,P00004,1,195000.0
O00010,P00006,3,36000.0
EOF

echo " [OK] Seed data successfully generated in $DATA_DIR"
