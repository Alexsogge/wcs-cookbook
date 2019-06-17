from django.contrib.auth.decorators import login_required
from django.http import JsonResponse, HttpResponseNotFound
from django.shortcuts import render
from rest_framework import viewsets, permissions, status
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


# def startNewSession(request, recipe_id):
#     current_user = request.user
#     print(current_user)
#     session = None
#     if CookingSession.objects.filter(owner=current_user, recipe__id=recipe_id).exists():
#         session = CookingSession.objects.get(owner=current_user, recipe__id=recipe_id)
#     else:
#         session = CookingSession.objects.create(owner=current_user, recipe=Recipe.objects.get(id=recipe_id))
#     return JsonResponse(SessionSerializer(session).data)

class startNewSession(viewsets.ModelViewSet):

    queryset = CookingSession.objects.all()
    serializer_class = SessionSerializer
    permission_classes = (permissions.IsAuthenticatedOrReadOnly,)

    def perform_create(self, serializer):
        print(self.request.user)

    def get_queryset(self):
        print(self.request.user)
        current_user = self.request.user
        recipe_id = self.request.GET['recipe_id']
        session = None
        if CookingSession.objects.filter(owner=current_user, recipe__id=recipe_id).exists():
            session = CookingSession.objects.get(owner=current_user, recipe__id=recipe_id)
        else:
            session = CookingSession.objects.create(owner=current_user, recipe=Recipe.objects.get(id=recipe_id))
        return [session,]

class GetActiveSessions(viewsets.ModelViewSet):

    queryset = CookingSession.objects.all()
    serializer_class = SessionSerializer
    permission_classes = (permissions.IsAuthenticatedOrReadOnly,)

    def get_queryset(self):
        print(self.request.user)
        current_user = self.request.user

        return CookingSession.objects.filter(owner_id=current_user)



class RegisterUser(APIView):

    def post(self, request, format=None):
        serializer = UserSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        else:
            return  JsonResponse({"error": "Not a valid user"})
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


def getWorkstep(request, step_id):
    print(step_id)
    return JsonResponse(WorkStepSerializer(WorkStep.objects.get(id=step_id)).data)


