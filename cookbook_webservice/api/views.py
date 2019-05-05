from django.http import JsonResponse, HttpResponseNotFound
from django.shortcuts import render
from rest_framework import viewsets
from api.serializers import *
from webportal.models import *
from rest_framework.authentication import SessionAuthentication, BasicAuthentication
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView


class TagViewSet(viewsets.ModelViewSet):
    queryset = Tag.objects.all()
    serializer_class = TagSerializer

class UnitViewSet(viewsets.ModelViewSet):
    queryset = Unit.objects.all()
    serializer_class = UnitSerializer

class IngredientViewSet(viewsets.ModelViewSet):
    queryset = Ingredient.objects.all()
    serializer_class = IngredientSerializer

class WorkStepViewSet(viewsets.ModelViewSet):
    queryset = WorkStep.objects.all()
    serializer_class = WorkStepSerializer

    def get_queryset(self):
        if 'recipe_id' in self.request.GET:
            return Recipe.objects.get(id=self.request.GET['recipe_id']).work_steps.all()
        return WorkStep.objects.all()

class RecipeViewSet(viewsets.ModelViewSet):
    queryset = Recipe.objects.all()
    serializer_class = RecipeSerializer

    def get_queryset(self):
        if 'id' in self.request.GET:
            return Recipe.objects.filter(id=self.request.GET['id'])
        return Recipe.objects.all()


def getRecipeSerialization(request, id):
    recipe = Recipe.objects.get(id=id)
    serial = RecipeSerializer(recipe)
    return JsonResponse(serial.data)

def getIngredient(request, id):
    ingredient = Ingredient.objects.get(id=id)
    serial = IngredientSerializer(ingredient)
    return JsonResponse(serial.data)

def startNewSession(request, recipe_id):
    current_user = request.user
    session = None
    if CookingSession.objects.filter(owner=current_user, recipe__id=recipe_id).exists():
        session = CookingSession.objects.get(owner=current_user, recipe__id=recipe_id)
    else:
        session = CookingSession.objects.create(owner=current_user, recipe=Recipe.objects.get(id=recipe_id))
    return JsonResponse(SessionSerializer(session).data)

