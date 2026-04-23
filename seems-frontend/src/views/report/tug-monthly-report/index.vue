<template>
  <div class="container">
    <Breadcrumb
      :items="['menu.report', 'menu.report.tugMonthlyReport']"
    />
    <a-card class="general-card" title="拖轮月度报表">
      <a-row>
        <a-col :flex="1">
          <a-form
            :model="formModel"
            :label-col-props="{ span: 4 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="right"
          >
            <a-row :gutter="64">
              <a-col :span="8">
                <a-form-item field="year" label="年份">
                  <a-select
                    v-model="formModel.year"
                    placeholder="请选择年份"
                    allow-search
                  >
                    <a-option
                      v-for="year in years"
                      :key="year"
                      :value="year"
                    >
                      {{ year }}
                    </a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="month" label="月份">
                  <a-select
                    v-model="formModel.month"
                    placeholder="请选择月份"
                  >
                    <a-option
                      v-for="month in months"
                      :key="month"
                      :value="month"
                    >
                      {{ month }}月
                    </a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="shipName" label="船舶">
                  <a-input v-model="shipName" disabled />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 42px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space direction="horizontal" :size="18">
            <a-button
              type="primary"
              :loading="loading"
              @click="handleGenerateReport"
            >
              <template #icon>
                <icon-search />
              </template>
              生成报表
            </a-button>
            <a-button
              v-if="reportData"
              type="outline"
              @click="handleExportPDF"
            >
              <template #icon>
                <icon-download />
              </template>
              导出PDF
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <div v-if="reportData" id="report-content">
      <!-- 统计数据卡片 -->
      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="总作业艘次"
              :value="reportData.totalOperations"
              suffix="次"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="总作业小时"
              :value="reportData.totalOperationHours"
              :precision="2"
              suffix="小时"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="单次平均作业时长"
              :value="reportData.averageOperationDuration"
              :precision="2"
              suffix="小时"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="累计航行里程"
              :value="reportData.totalVoyageDistance"
              :precision="2"
              suffix="海里"
            />
          </a-card>
        </a-col>
      </a-row>

      <!-- 能耗统计表格 -->
      <a-card title="能耗统计" style="margin-top: 16px">
        <a-table
          :data="energyTableData"
          :pagination="false"
          :bordered="true"
          size="medium"
        >
          <template #columns>
            <a-table-column title="项目" data-index="item" />
            <a-table-column title="数值" data-index="value" />
            <a-table-column title="备注/单位" data-index="unit" />
          </template>
        </a-table>
      </a-card>

      <!-- 每日能耗折线图 -->
      <a-card title="每日能耗趋势" style="margin-top: 16px">
        <div ref="chartRef" style="height: 400px"></div>
      </a-card>

      <!-- 每日详细数据表格 -->
      <a-card title="每日详细数据" style="margin-top: 16px">
        <a-table
          :data="reportData.dailyEnergyConsumptions"
          :pagination="{ pageSize: 10 }"
          :bordered="true"
          size="medium"
        >
          <template #columns>
            <a-table-column title="日期" data-index="date" />
            <a-table-column title="能耗(kWh)" data-index="energyConsumption" :precision="2" />
            <a-table-column title="作业艘次" data-index="operations" />
            <a-table-column title="航行里程(海里)" data-index="voyageDistance" :precision="2" />
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue';
  import { useI18n } from 'vue-i18n';
  import useLoading from '@/hooks/loading';
  import { Message } from '@arco-design/web-vue';
  import {
    generateTugMonthlyReport,
    TugMonthlyReport,
    TugMonthlyReportQuery,
  } from '@/api/tug-monthly-report';
  import * as echarts from 'echarts';
  import { getShipInfo } from '@/api/ship';
  import html2canvas from 'html2canvas';
  import jsPDF from 'jspdf';
  import '@/assets/fonts/AlibabaPuHuiTi-3-55-Regular-normal'

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(false);
  const chartRef = ref<HTMLDivElement>();
  const chartInstance = ref<echarts.ECharts>();
  const reportData = ref<TugMonthlyReport>();

  const shipName = ref('');
  const shipId = ref<number>(0);
  const shipInfo = ref<Record<string, any>>({});

  const years = ref<number[]>((() => {
    const currentYear = new Date().getFullYear();
    return Array.from({ length: 5 }, (_, i) => currentYear - i);
  })());

  const months = ref<number[]>(Array.from({ length: 12 }, (_, i) => i + 1));

  const formModel = reactive({
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
  });

  const energyTableData = computed(() => {
    if (!reportData.value) return [];
    return [
      {
        item: '月度总耗电量',
        value: reportData.value.totalPowerConsumption.toLocaleString(),
        unit: 'kWh',
      },
      {
        item: '单船平均能耗',
        value: reportData.value.averagePowerConsumptionPerOperation.toFixed(2),
        unit: 'kWh/艘次',
      },
      {
        item: '每海里能耗',
        value: reportData.value.powerConsumptionPerNauticalMile.toFixed(2),
        unit: 'kWh/海里',
      },
    ];
  });

  const fetchShipInfo = async () => {
    try {
      const { data } = await getShipInfo();
      shipId.value = data.id;
      shipName.value = data.name;
      shipInfo.value = data;
    } catch (err) {
      Message.error('获取船舶信息失败');
    }
  };

  const handleGenerateReport = async () => {
    if (!shipId.value) {
      Message.warning('无法获取船舶信息');
      return;
    }
    if (!formModel.year || !formModel.month) {
      Message.warning('请选择年份和月份');
      return;
    }

    setLoading(true);
    try {
      const query: TugMonthlyReportQuery = {
        shipId: shipId.value,
        year: formModel.year,
        month: formModel.month,
      };
      const { data } = await generateTugMonthlyReport(query);
      reportData.value = data;
      await nextTick();
      initChart();
    } catch (err) {
      Message.error('生成报表失败');
    } finally {
      setLoading(false);
    }
  };

  const initChart = () => {
    if (!reportData.value || !chartRef.value) return;

    if (chartInstance.value) {
      chartInstance.value.dispose();
    }

    chartInstance.value = echarts.init(chartRef.value);

    const dates = reportData.value.dailyEnergyConsumptions.map((d) => d.date);
    const energyData = reportData.value.dailyEnergyConsumptions.map((d) => d.energyConsumption);

    const option = {
      title: {
        text: `${reportData.value.year}年${reportData.value.month}月每日能耗`,
        left: 'center',
      },
      tooltip: {
        trigger: 'axis',
      },
      xAxis: {
        type: 'category',
        data: dates,
        axisLabel: {
          rotate: 45,
        },
      },
      yAxis: {
        type: 'value',
        name: '能耗 (kWh)',
      },
      series: [
        {
          name: '能耗',
          type: 'line',
          data: energyData,
          smooth: true,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
              { offset: 1, color: 'rgba(24, 144, 255, 0.05)' },
            ]),
          },
          itemStyle: {
            color: '#1890ff',
          },
        },
      ],
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
    };

    chartInstance.value.setOption(option);
  };

  const handleExportPDF = async () => {
    if (!reportData.value) return;

    try {
      const pdf = new jsPDF('p', 'mm', 'a4');
      pdf.addFont('AlibabaPuHuiTi-3-55-Regular-normal.ttf', 'AlibabaPuHuiTi-3-55-Regular', 'normal');
      pdf.setFont("AlibabaPuHuiTi-3-55-Regular");
      const pageWidth = pdf.internal.pageSize.getWidth();
      const pageHeight = pdf.internal.pageSize.getHeight();
      const margin = 20;
      let currentY = margin;

      const addTitle = () => {
        pdf.setFontSize(22);
        pdf.text('拖轮月度报表', pageWidth / 2, currentY, { align: 'center' });
        currentY += 15;
      };

      const addBasicInfo = () => {
        pdf.setFontSize(14);
        pdf.text('基础信息', margin, currentY);
        currentY += 8;

        pdf.setFontSize(11);
        const lineHeight = 7;
        const colWidth = 80;

        pdf.text(`拖轮名称: ${reportData.value.shipName}`, margin, currentY);
        pdf.text(`MMSI编号: ${shipInfo.value.mmsi || 'N/A'}`, margin + colWidth, currentY);
        currentY += lineHeight;

        pdf.text(`船舶ID: ${reportData.value.shipId}`, margin, currentY);
        pdf.text(`报表周期: ${reportData.value.year}年${reportData.value.month}月`, margin + colWidth, currentY);
        currentY += lineHeight;

        if (shipInfo.value.type) {
          pdf.text(`船舶类型: ${shipInfo.value.type}`, margin, currentY);
          currentY += lineHeight;
        }
        if (shipInfo.value.length) {
          pdf.text(`船舶长度: ${shipInfo.value.length}米`, margin, currentY);
          currentY += lineHeight;
        }
        if (shipInfo.value.tonnage) {
          pdf.text(`船舶吨位: ${shipInfo.value.tonnage}吨`, margin, currentY);
          currentY += lineHeight;
        }

        currentY += 5;
      };

      const addEnergyTable = () => {
        pdf.setFontSize(14);
        pdf.text('能效数据统计', margin, currentY);
        currentY += 8;

        const tableData = [
          ['项目', '数值', '单位'],
          ['总作业艘次', reportData.value.totalOperations.toLocaleString(), '次'],
          ['总作业小时', reportData.value.totalOperationHours.toFixed(2), '小时'],
          ['单次平均作业时长', reportData.value.averageOperationDuration.toFixed(2), '小时'],
          ['累计航行里程', reportData.value.totalVoyageDistance.toFixed(2), '海里'],
          ['月度总耗电量', reportData.value.totalPowerConsumption.toLocaleString(), 'kWh'],
          ['单船平均能耗', reportData.value.averagePowerConsumptionPerOperation.toFixed(2), 'kWh/艘次'],
          ['每海里能耗', reportData.value.powerConsumptionPerNauticalMile.toFixed(2), 'kWh/海里'],
        ];

        const cellWidth = (pageWidth - 2 * margin) / 3;
        const cellHeight = 8;

        tableData.forEach((row, rowIndex) => {
          row.forEach((cell, colIndex) => {
            const x = margin + colIndex * cellWidth;
            const y = currentY + rowIndex * cellHeight;

            if (rowIndex === 0) {
              pdf.setFillColor(24, 144, 255);
              pdf.rect(x, y, cellWidth, cellHeight, 'F');
              pdf.setTextColor(255, 255, 255);
            } else {
              if (rowIndex % 2 === 0) {
                pdf.setFillColor(245, 245, 245);
                pdf.rect(x, y, cellWidth, cellHeight, 'F');
              }
              pdf.setTextColor(0, 0, 0);
            }

            pdf.setFontSize(10);
            pdf.text(cell, x + 2, y + 5.5);
          });
        });

        currentY += tableData.length * cellHeight + 10;
      };

      const addDailyData = () => {
        if (currentY + 30 > pageHeight) {
          pdf.addPage();
          currentY = margin;
        }

        pdf.setFontSize(14);
        pdf.text('每日详细数据', margin, currentY);
        currentY += 8;

        const dailyData = reportData.value.dailyEnergyConsumptions;
        const tableData = [
          ['日期', '能耗(kWh)', '作业艘次', '航行里程(海里)'],
          ...dailyData.map(d => [
            d.date,
            d.energyConsumption.toFixed(2),
            d.operations.toString(),
            d.voyageDistance.toFixed(2)
          ])
        ];

        const cellWidth = (pageWidth - 2 * margin) / 4;
        const cellHeight = 6;
        const rowsPerPage = Math.floor((pageHeight - currentY - 20) / cellHeight);
        let currentRow = 0;
        let totalRowsDrawn = 0;

        while (currentRow < tableData.length) {
          const remainingRows = tableData.length - currentRow;
          const rowsToDraw = Math.min(remainingRows, rowsPerPage);

          for (let i = 0; i < rowsToDraw; i++) {
            const rowIndex = currentRow + i;
            const row = tableData[rowIndex];

            row.forEach((cell, colIndex) => {
              const x = margin + colIndex * cellWidth;
              const y = currentY + i * cellHeight;

              if (rowIndex === 0) {
                pdf.setFillColor(24, 144, 255);
                pdf.rect(x, y, cellWidth, cellHeight, 'F');
                pdf.setTextColor(255, 255, 255);
              } else {
                if (rowIndex % 2 === 0) {
                  pdf.setFillColor(245, 245, 245);
                  pdf.rect(x, y, cellWidth, cellHeight, 'F');
                }
                pdf.setTextColor(0, 0, 0);
              }

              pdf.setFontSize(8);
              pdf.text(cell, x + 2, y + 4);
            });
          }

          currentRow += rowsToDraw;
          totalRowsDrawn += rowsToDraw;

          if (currentRow < tableData.length) {
            pdf.addPage();
            currentY = margin;
          }
        }

        currentY += totalRowsDrawn * cellHeight + 10;
      };

      const addChartImage = async () => {
        if (!chartRef.value) return;

        if (currentY + 50 > pageHeight) {
          pdf.addPage();
          currentY = margin;
        }

        pdf.setFontSize(14);
        pdf.text('每日能耗趋势图', margin, currentY);
        currentY += 8;

        try {
          const canvas = await html2canvas(chartRef.value, {
            scale: 2,
            useCORS: true,
            logging: false,
            backgroundColor: '#ffffff'
          });

          const imgData = canvas.toDataURL('image/png');
          const imgWidth = pageWidth - 2 * margin;
          const imgHeight = (canvas.height * imgWidth) / canvas.width;

          if (currentY + imgHeight > pageHeight - 10) {
            pdf.addPage();
            currentY = margin;
          }

          pdf.addImage(imgData, 'PNG', margin, currentY, imgWidth, imgHeight);
        } catch (err) {
          console.error('图表导出失败:', err);
        }
      };

      addTitle();
      addBasicInfo();
      addEnergyTable();
      addDailyData();
      await addChartImage();

      const fileName = `拖轮月度报表_${reportData.value.shipName}_${reportData.value.year}年${reportData.value.month}月.pdf`;
      pdf.save(fileName);
      Message.success('PDF导出成功');
    } catch (err) {
      Message.error('PDF导出失败');
      console.error(err);
    }
  };

  const handleResize = () => {
    if (chartInstance.value) {
      chartInstance.value.resize();
    }
  };

  onMounted(() => {
    fetchShipInfo();
    window.addEventListener('resize', handleResize);
  });

  onUnmounted(() => {
    if (chartInstance.value) {
      chartInstance.value.dispose();
    }
    window.removeEventListener('resize', handleResize);
  });
</script>

<style scoped lang="less">
  .container {
    padding: 0 20px 20px;
  }

  :deep(.arco-statistic) {
    .arco-statistic-title {
      font-size: 14px;
      color: var(--color-text-2);
    }

    .arco-statistic-content {
      margin-top: 8px;

      .arco-statistic-value {
        font-size: 24px;
        font-weight: 600;
        color: rgb(var(--primary-6));
      }
    }
  }
</style>
