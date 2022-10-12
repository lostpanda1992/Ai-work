# -- coding: utf-8 -- 
# @Time : 2021/9/13 15:46
# @Author : Jonny.chen
# @File : new_factory.py

from image_models.asl.models.tresnet import TResnetM, TResnetL, TResnetXL


def create_model(args):
    """Create a model
    """
    model_params = args
    # args = model_params['args']
    # args.model_name = args.model_name.lower()

    if args["model_name"]=='tresnet_m':
        model = TResnetM(model_params)
    elif args["model_name"]=='tresnet_l':
        model = TResnetL(model_params)
    elif args["model_name"]=='tresnet_xl':
        model = TResnetXL(model_params)
    else:
        print("model: {} not found !!".format(args["model_name"]))
        exit(-1)

    return model