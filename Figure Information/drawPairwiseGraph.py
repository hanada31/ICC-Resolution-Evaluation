# sphinx_gallery_thumbnail_number = 3
import matplotlib.pyplot as plt
from matplotlib.pyplot import MultipleLocator
import numpy as np


def plot_graph(model_a, model_b, x, y, z, color_a, color_b):
    sub = plt.subplot(x, y, z)
    plt.ylim((0,1))
    y_major_locator=MultipleLocator(1)
    ax=plt.gca()
    ax.yaxis.set_major_locator(y_major_locator)

    plt.rcParams['xtick.direction'] = 'inout'
    plt.rcParams['ytick.direction'] = 'in'

    names = list()
    names.append(model_a[0])
    names.append(model_b[0])

    m = 1
    common = list()
    common.append(model_a[1] * m)
    common.append(model_b[1] * m)

    only = list()
    only.append(model_a[2] * m)
    only.append(model_b[2] * m)

    missed = list()
    missed.append(model_a[3] * m)
    missed.append(model_b[3] * m)

    width = 0.5
    plt.bar(names, common, width=width, color = color_a, label='common')
    plt.bar(names, only, width=width, color = color_b, bottom=common, label='only')
    plt.rc('xtick', labelsize=12)
    plt.rc('ytick', labelsize=12)

    return (sub,plt)

def drawAllICC():
    color_a = 'steelblue'
    color_b = 'orange'
    model_a = [	'IC3'	,	0.134193548	,	0.34	,	0.525806452	]
    model_b = [	'Gator'	,	0.134193548	,	0.525806452	,	0.34	]


    (sub,plt) = plot_graph(model_a, model_b, 6, 6, 7, color_a, color_b)
    plt.ylabel('IC3', {'size': 'large'})
    model_a = [	'IC3Dial'	,	0.054666667	,	0.107	,	0.838666667	]
    model_b = [	'Gator'	,	0.054666667	,	0.838666667	,	0.107	]
    (sub,plt) = plot_graph(model_a, model_b, 6, 6, 13, color_a, color_b)
    plt.ylabel('IC3Dial', {'size': 'large'})

    model_a = [	'A3E'	,	0.113548387	,	0.246774194	,	0.64	]
    model_b = [	'Gator'	,	0.113548387	,	0.64	,	0.246774194	]
    (sub,plt) = plot_graph(model_a, model_b, 6, 6,19, color_a, color_b)
    plt.ylabel('A3E', {'size': 'large'})

    model_a = [	'StoryD'	,	0.202258065	,	0.257096774	,	0.539677419	]
    model_b = [	'Gator'	,	0.202258065	,	0.539677419	,	0.257096774	]
    (sub,plt) = plot_graph(model_a, model_b, 6, 6, 25, color_a, color_b)
    plt.ylabel('StoryD', {'size': 'large'})

    model_a = [	'ICCBot'	,	0.228709677	,	0.496129032	,	0.275806452	]
    model_b = [	'Gator'	,	0.228709677	,	0.275806452	,	0.496129032	]
    plot_graph(model_a, model_b, 6, 6, 31, color_a, color_b)
    plt.ylabel('ICCBot', {'size': 'large'})

    model_a = [	'IC3Dial'	,	0.409090909	,	0	,	0.590909091	]
    model_b = [	'IC3'	,	0.409090909	,	0.590909091	,	0	]

    plot_graph(model_a, model_b, 6, 6, 14, color_a, color_b)

    model_a = [	'A3E'	,	0.173448276	,	0.347241379	,	0.480689655	]
    model_b = [	'IC3'	,	0.173448276	,	0.480689655	,	0.347241379	]
    plot_graph(model_a, model_b, 6, 6, 20, color_a, color_b)

    model_a = [	'StoryD'	,	0.304666667	,	0.380333333	,	0.315333333	]
    model_b = [	'IC3'	,	0.304666667	,	0.315333333	,	0.380333333	]
    plot_graph(model_a, model_b, 6, 6, 26, color_a, color_b)

    model_a = [	'ICCBot'	,	0.200967742	,	0.619677419	,	0.179677419	]
    model_b = [	'IC3'	,	0.200967742	,	0.179677419	,	0.619677419	]
    plot_graph(model_a, model_b, 6, 6, 32, color_a, color_b)


    model_a = [	'A3E'	,	0.090344828	,	0.760344828	,	0.149655172	]
    model_b = [	'IC3Dial'	,	0.090344828	,	0.149655172	,	0.760344828	]
    plot_graph(model_a, model_b, 6, 6, 21, color_a, color_b)

    model_a = [	'StoryD'	,	0.188333333	,	0.767	,	0.044666667	]
    model_b = [	'IC3Dial'	,	0.188333333	,	0.044666667	,	0.767	]
    plot_graph(model_a, model_b, 6, 6, 27, color_a, color_b)

    model_a = [	'ICCBot'	,	0.09	,	0.878064516	,	0.032258065	]
    model_b = [	'IC3Dial'	,	0.09	,	0.032258065	,	0.878064516	]
    plot_graph(model_a, model_b, 6, 6, 33, color_a, color_b)

    model_a = [	'StoryD'	,	0.295483871	,	0.449032258	,	0.255483871	]
    model_b = [	'A3E'	,	0.295483871	,	0.255483871	,	0.449032258	]
    plot_graph(model_a, model_b, 6, 6, 28, color_a, color_b)

    model_a = [	'ICCBot'	,	0.216774194	,	0.76483871	,	0.018387097	]
    model_b = [	'A3E'	,	0.216774194	,	0.018387097	,	0.76483871	]
    plot_graph(model_a, model_b, 6, 6, 34, color_a, color_b)

    model_a = [	'ICCBot'	,	0.268709677	,	0.667096774	,	0.064193548	]
    model_b = [	'StoryD'	,	0.268709677	,	0.064193548	,	0.667096774	]
    plot_graph(model_a, model_b, 6, 6, 35, color_a, color_b)



def drawTPICC():
    color_a = 'seagreen'
    color_b = 'salmon'
    model_a = [	'IC3'	,	0.167741935	,	0.150322581	,	0.682580645	]
    model_b = [	'Gator'	,	0.167741935	,	0.264516129	,	0.568709677	]
    (sub,plt) = plot_graph(model_b, model_a, 6, 6, 2, color_a, color_b)
    sub.set_title('IC3')

    model_a = [	'IC3Dial'	,	0.066774194	,	0.038064516	,	0.895483871	]
    model_b = [	'Gator'	,	0.066774194	,	0.364516129	,	0.568709677	]
    (sub,plt) = plot_graph(model_b, model_a, 6, 6, 3, color_a, color_b)
    sub.set_title('IC3Dial')

    model_a = [	'A3E'	,	0.107419355	,	0.124193548	,	0.769354839	]
    model_b = [	'Gator'	,	0.107419355	,	0.32483871	,	0.568709677	]
    (sub,plt) = plot_graph(model_b, model_a, 6, 6,4, color_a, color_b)
    sub.set_title('A3E')

    model_a = [	'StoryD'	,	0.218709677	,	0.152258065	,	0.629354839	]
    model_b = [	'Gator'	,	0.218709677	,	0.212580645	,	0.568709677	]
    (sub,plt) = plot_graph(model_b, model_a, 6, 6, 5, color_a, color_b)
    sub.set_title('StoryD')

    model_a = [	'ICCBot'	,	0.36	,	0.429032258	,	0.211612903	]
    model_b = [	'Gator'	,	0.36	,	0.070967742	,	0.568709677	]
    (sub,plt) = plot_graph(model_b, model_a, 6, 6, 6, color_a, color_b)
    sub.set_title('ICCBot')

    model_a = [	'IC3Dial'	,	0.10483871	,	0	,	0.895483871	]
    model_b = [	'IC3'	,	0.10483871	,	0.212903226	,	0.682580645	]
    plot_graph(model_b, model_a, 6, 6, 9, color_a, color_b)

    model_a = [	'A3E'	,	0.14483871	,	0.086129032	,	0.769354839	]
    model_b = [	'IC3'	,	0.14483871	,	0.172580645	,	0.682580645	]
    plot_graph(model_b, model_a, 6, 6, 10, color_a, color_b)

    model_a = [	'StoryD'	,	0.197741935	,	0.172903226	,	0.629354839	]
    model_b = [	'IC3'	,	0.197741935	,	0.120322581	,	0.682580645	]
    plot_graph(model_b, model_a, 6, 6, 11, color_a, color_b)

    model_a = [	'ICCBot'	,	0.297419355	,	0.490967742	,	0.211612903	]
    model_b = [	'IC3'	,	0.297419355	,	0.020322581	,	0.682580645	]
    plot_graph(model_b, model_a, 6, 6, 12, color_a, color_b)

    model_a = [	'A3E'	,	0.058709677	,	0.172258065	,	0.769354839	]
    model_b = [	'IC3Dial'	,	0.058709677	,	0.045806452	,	0.895483871	]
    plot_graph(model_b, model_a, 6, 6, 16, color_a, color_b)

    model_a = [	'StoryD'	,	0.090322581	,	0.280322581	,	0.629354839	]
    model_b = [	'IC3Dial'	,	0.090322581	,	0.014193548	,	0.895483871	]
    plot_graph(model_b, model_a, 6, 6, 17, color_a, color_b)

    model_a = [	'ICCBot'	,	0.099677419	,	0.689032258	,	0.211612903	]
    model_b = [	'IC3Dial'	,	0.099677419	,	0.00516129	,	0.895483871	]
    plot_graph(model_b, model_a, 6, 6, 18, color_a, color_b)

    model_a = [	'StoryD'	,	0.17483871	,	0.196451613	,	0.629354839	]
    model_b = [	'A3E'	,	0.17483871	,	0.056451613	,	0.769354839	]
    plot_graph(model_b, model_a, 6, 6, 23, color_a, color_b)

    model_a = [	'ICCBot'	,	0.217096774	,	0.571290323	,	0.211612903	]
    model_b = [	'A3E'	,	0.217096774	,	0.013548387	,	0.769354839	]
    plot_graph(model_b, model_a, 6, 6, 24, color_a, color_b)

    model_a = [	'ICCBot'	,	0.353548387	,	0.43483871	,	0.211612903	]
    model_b = [	'StoryD'	,	0.353548387	,	0.016774194	,	0.629354839	]
    plot_graph(model_b, model_a, 6, 6, 30, color_a, color_b)

def drawLegend():
    sub = plt.subplot(6,6,1)
    sub.set_title('Gator')
    sub.spines['right'].set_visible(False)
    sub.spines['top'].set_visible(False)
    sub.spines['left'].set_visible(False)
    sub.spines['bottom'].set_visible(False)
    plt.xticks([])
    plt.yticks([])
    plt.ylabel('Gator', {'size': 'large'})

    names = ['ALa', 'ALb','TPa', 'TPb']
    ALa = ALb = TPa = TPb = [0,0,0,0]
    plt.rcParams['legend.fontsize'] = 10
    plt.bar(names, ALa, color = ['steelblue'], label='ALLcommom')
    plt.bar(names, ALb, color = ['orange'], bottom=ALa, label='ALLonly')
    plt.bar(names, TPa, color = ['seagreen'], bottom=ALb, label='TPcommon')
    plt.bar(names, TPb, color = ['salmon'], bottom=TPa, label='TPonly')
    plt.legend()

if __name__ == '__main__':
    plt.figure(figsize=(12,10))

    drawLegend()
    drawAllICC()
    drawTPICC()

    plt.subplots_adjust(left=0.07, bottom=0.2, right=0.93, top=0.8, \
    wspace=0.2, hspace=0.37)
    plt.suptitle('Pairwise Graph')

    # plt.show()

    plt.savefig("pair.pdf", format="pdf")