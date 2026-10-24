# ==============================================================================
# Food Store Management System - Dynamic Date Data Seeder (PowerShell)
# Generates realistic sample CSV data relative to the current execution date.
# ==============================================================================

$today = Get-Date
$dataDir = Join-Path $PSScriptRoot "data"
if (-not (Test-Path $dataDir)) {
    New-Item -ItemType Directory -Path $dataDir -Force | Out-Null
}

Write-Host "Seeding Food Store Management System sample data based on current date: $($today.ToString('yyyy-MM-dd'))..." -ForegroundColor Cyan

# 1. products.csv (18 products: 14 active, 4 soft-deleted)
$products = @(
    "id,name,category,unit,price,isDeleted",
    "P00001,ST25 Fragrant Rice 5kg,Grains,Bag,165000.0,false",
    "P00002,Hao Hao Sour Spicy Noodles,Noodles,Pack,4500.0,false",
    "P00003,Australian Beef Sirloin 500g,Meat,Tray,145000.0,false",
    "P00004,Norwegian Salmon Fillet 300g,Seafood,Tray,195000.0,true",
    "P00005,Dalat Hydroponic Lettuce,Produce,Bag,22000.0,false",
    "P00006,Vinamilk Fresh Milk 1L,Dairy,Carton,36000.0,false",
    "P00007,CP Pork & Shrimp Dumplings,Frozen,Pack,68000.0,false",
    "P00008,Three Lady Cooks Canned Tuna,Canned,Can,32000.0,true",
    "P00009,La Vie Mineral Water 1.5L,Beverages,Bottle,24000.0,false",
    "P00010,Nam Ngu Phu Quoc Fish Sauce,Condiments,Bottle,48000.0,false",
    "P00011,Oishi Prawn Crackers 80g,Snacks,Pack,26000.0,false",
    "P00012,ABC Whole Wheat Sandwich Bread,Bakery,Pack,25000.0,false",
    "P00013,Discontinued French Butter,Dairy,Box,85000.0,true",
    "P00014,Chinsu Chili Sauce 250g,Condiments,Bottle,18000.0,false",
    "P00015,Dalat Fresh Strawberries 250g,Produce,Box,65000.0,false",
    "P00016,Expired Seaweed Snacks,Snacks,Pack,15000.0,true",
    "P00017,Heineken Beer Can 330ml,Beverages,Can,21000.0,false",
    "P00018,Mezan Pure Soybean Oil 1L,Condiments,Bottle,52000.0,false"
)
[System.IO.File]::WriteAllLines((Join-Path $dataDir "products.csv"), $products, [System.Text.Encoding]::ASCII)

# 2. customers.csv (13 customers: 11 active, 2 soft-deleted, all in HCM)
$customers = @(
    "id,fullName,phone,address,customerType,isDeleted",
    'C00001,Nguyen Van An,0901234567,"12 Le Loi, Ben Nghe, Q1, Ho Chi Minh",VIP,false',
    'C00002,Tran Thi Binh,0912345678,"45 Nguyen Hue, Q1, HCM",Regular,false',
    'C00003,Le Hoang Minh,0987654321,"108 Dien Bien Phu, P6, Q3, TP.HCM",VIP,false',
    'C00004,Pham Thu Ha,0938112233,"22 Phan Xich Long, Phu Nhuan, HCM",Regular,false',
    'C00005,Vo Quoc Bao,0979445566,"88 Nguyen Van Linh, Tan Phong, Q7, Ho Chi Minh",VIP,false',
    'C00006,Dang Minh Tuan,0909887766,"15 Cach Mang Thang 8, P10, Q3, HCM",Regular,false',
    'C00007,Hoang Gia Huy,0932998877,"30 Ly Tu Trong, Q1, Ho Chi Minh",Regular,true',
    'C00008,Tran Cao Thanh,0372240629,"Duong 1a, Linh Trung, Thu Duc, HCM",VIP,false',
    'C00009,Nguyen Van A,0372240628,"2 Ba Trung, Ben Nghe, Q1, HCM",Regular,false',
    'C00010,Nguyen Van B,0372240627,"12/3A Nguyen Trai, P2, Q5, TP.HCM",Regular,false',
    'C00011,Do Thi Mai,0903123456,"55 Vo Van Tan, P6, Q3, Ho Chi Minh",VIP,false',
    'C00012,Pham Thanh Thao,0944556677,"15 Le Duan, Ben Nghe, Q1, HCM",VIP,true',
    'C00013,Bui Hoang Nam,0988776655,"102 Nguyen Thi Minh Khai, Q1, HCM",Regular,false'
)
[System.IO.File]::WriteAllLines((Join-Path $dataDir "customers.csv"), $customers, [System.Text.Encoding]::ASCII)

# Helper date strings
$d_m30 = $today.AddDays(-30).ToString("yyyy-MM-dd")
$d_m25 = $today.AddDays(-25).ToString("yyyy-MM-dd")
$d_m20 = $today.AddDays(-20).ToString("yyyy-MM-dd")
$d_m15 = $today.AddDays(-15).ToString("yyyy-MM-dd")
$d_m10 = $today.AddDays(-10).ToString("yyyy-MM-dd")
$d_m5  = $today.AddDays(-5).ToString("yyyy-MM-dd")
$d_m2  = $today.AddDays(-2).ToString("yyyy-MM-dd")
$d_now = $today.ToString("yyyy-MM-dd")
$d_p3  = $today.AddDays(3).ToString("yyyy-MM-dd")
$d_p15 = $today.AddDays(15).ToString("yyyy-MM-dd")
$d_p30 = $today.AddDays(30).ToString("yyyy-MM-dd")
$d_p60 = $today.AddDays(60).ToString("yyyy-MM-dd")
$d_p90 = $today.AddDays(90).ToString("yyyy-MM-dd")
$d_p180= $today.AddDays(180).ToString("yyyy-MM-dd")

# 3. batches.csv (active, low-stock, near-expiry <= 7 days, and expired batches)
$batches = @(
    "batchId,productId,importDate,productionDate,expirationDate,quantity",
    "B00001,P00001,$d_m5,$d_m20,$d_p180,45",
    "B00002,P00001,$d_m2,$d_m15,$d_p180,15",
    "B00003,P00002,$d_m10,$d_m25,$d_p90,100",
    "B00004,P00002,$d_m5,$d_m15,$d_p90,40",
    "B00005,P00003,$d_m5,$d_m10,$d_p15,4",
    "B00006,P00005,$d_m2,$d_m5,$d_p3,12",
    "B00007,P00006,$d_m10,$d_m15,$d_p30,20",
    "B00008,P00006,$d_m5,$d_m10,$d_p30,12",
    "B00009,P00007,$d_m15,$d_m30,$d_p90,18",
    "B00010,P00009,$d_m10,$d_m25,$d_p180,48",
    "B00011,P00010,$d_m20,$d_m30,$d_p180,22",
    "B00012,P00011,$d_m10,$d_m20,$d_p90,40",
    "B00013,P00012,$d_m5,$d_m10,$d_p3,3",
    "B00014,P00014,$d_m15,$d_m25,$d_p180,30",
    "B00015,P00015,$d_m2,$d_m5,$d_p3,8",
    "B00016,P00017,$d_m10,$d_m20,$d_p180,60",
    "B00017,P00018,$d_m15,$d_m25,$d_p180,25",
    "B00018,P00006,$d_m30,$d_m30,$d_m2,5",
    "B00019,P00012,$d_m25,$d_m30,$d_m2,2"
)
[System.IO.File]::WriteAllLines((Join-Path $dataDir "batches.csv"), $batches, [System.Text.Encoding]::ASCII)

# Dynamic order datetime stamps (15 orders across the current month, including today)
$t_today_m1 = $today.Date.AddHours(9).AddMinutes(15).ToString("yyyy-MM-ddTHH:mm:ss")
$t_today_m2 = $today.Date.AddHours(11).AddMinutes(30).ToString("yyyy-MM-ddTHH:mm:ss")
$t_today_m3 = $today.Date.AddHours(14).AddMinutes(45).ToString("yyyy-MM-ddTHH:mm:ss")
$t_d1_1 = $today.AddDays(-1).Date.AddHours(10).AddMinutes(20).ToString("yyyy-MM-ddTHH:mm:ss")
$t_d1_2 = $today.AddDays(-1).Date.AddHours(16).AddMinutes(10).ToString("yyyy-MM-ddTHH:mm:ss")
$t_d2_1 = $today.AddDays(-2).Date.AddHours(15).AddMinutes(30).ToString("yyyy-MM-ddTHH:mm:ss")
$t_d3_1 = $today.AddDays(-3).Date.AddHours(8).AddMinutes(45).ToString("yyyy-MM-ddTHH:mm:ss")
$t_d4_1 = $today.AddDays(-4).Date.AddHours(11).AddMinutes(0).ToString("yyyy-MM-ddTHH:mm:ss")
$t_d5_1 = $today.AddDays(-5).Date.AddHours(17).AddMinutes(15).ToString("yyyy-MM-ddTHH:mm:ss")
$t_d6_1 = $today.AddDays(-6).Date.AddHours(14).AddMinutes(20).ToString("yyyy-MM-ddTHH:mm:ss")
$t_d7_1 = $today.AddDays(-7).Date.AddHours(10).AddMinutes(30).ToString("yyyy-MM-ddTHH:mm:ss")
$t_d8_1 = $today.AddDays(-8).Date.AddHours(12).AddMinutes(15).ToString("yyyy-MM-ddTHH:mm:ss")
$t_d10_1 = $today.AddDays(-10).Date.AddHours(16).AddMinutes(45).ToString("yyyy-MM-ddTHH:mm:ss")
$t_d12_1 = $today.AddDays(-12).Date.AddHours(9).AddMinutes(30).ToString("yyyy-MM-ddTHH:mm:ss")
$t_d15_1 = $today.AddDays(-15).Date.AddHours(15).AddMinutes(0).ToString("yyyy-MM-ddTHH:mm:ss")

# 4. orders.csv (15 completed orders)
$orders = @(
    "orderId,customerId,orderDate,totalAmount,discount,finalAmount",
    "O00001,C00001,$t_d15_1,378000.0,37800.0,340200.0",
    "O00002,C00002,$t_d12_1,117000.0,0.0,117000.0",
    "O00003,C00003,$t_d10_1,485000.0,48500.0,436500.0",
    "O00004,GUEST,$t_d8_1,124000.0,0.0,124000.0",
    "O00005,C00001,$t_d7_1,351000.0,35100.0,315900.0",
    "O00006,C00005,$t_d6_1,194000.0,19400.0,174600.0",
    "O00007,C00006,$t_d5_1,72000.0,0.0,72000.0",
    "O00008,C00008,$t_d4_1,330000.0,33000.0,297000.0",
    "O00009,GUEST,$t_d3_1,90000.0,0.0,90000.0",
    "O00010,C00011,$t_d2_1,233000.0,23300.0,209700.0",
    "O00011,C00009,$t_d1_1,45000.0,0.0,45000.0",
    "O00012,C00004,$t_d1_2,136000.0,0.0,136000.0",
    "O00013,C00001,$t_today_m1,183000.0,18300.0,164700.0",
    "O00014,GUEST,$t_today_m2,54000.0,0.0,54000.0",
    "O00015,C00003,$t_today_m3,290000.0,29000.0,261000.0"
)
[System.IO.File]::WriteAllLines((Join-Path $dataDir "orders.csv"), $orders, [System.Text.Encoding]::ASCII)

# 5. order_details.csv (Line items for 15 orders)
$details = @(
    "orderId,productId,quantity,unitPrice",
    "O00001,P00001,2,165000.0",
    "O00001,P00010,1,48000.0",
    "O00002,P00005,2,22000.0",
    "O00002,P00006,2,36000.0",
    "O00002,P00002,2,4500.0",
    "O00003,P00003,2,145000.0",
    "O00003,P00004,1,195000.0",
    "O00004,P00009,3,24000.0",
    "O00004,P00011,2,26000.0",
    "O00005,P00001,1,165000.0",
    "O00005,P00007,2,68000.0",
    "O00005,P00012,2,25000.0",
    "O00006,P00008,4,32000.0",
    "O00006,P00015,1,65000.0",
    "O00007,P00006,2,36000.0",
    "O00008,P00001,2,165000.0",
    "O00009,P00002,10,4500.0",
    "O00009,P00011,1,26000.0",
    "O00009,P00014,1,18000.0",
    "O00010,P00003,1,145000.0",
    "O00010,P00005,4,22000.0",
    "O00011,P00002,10,4500.0",
    "O00012,P00007,2,68000.0",
    "O00013,P00001,1,165000.0",
    "O00013,P00014,1,18000.0",
    "O00014,P00011,1,26000.0",
    "O00014,P00017,1,21000.0",
    "O00014,P00002,1,7000.0",
    "O00015,P00003,2,145000.0"
)
[System.IO.File]::WriteAllLines((Join-Path $dataDir "order_details.csv"), $details, [System.Text.Encoding]::ASCII)

Write-Host "Data seeding completed successfully! Generated: 18 products, 13 customers, 19 batches, 15 orders." -ForegroundColor Green
