# coding: utf-8

"""
    AllOfUs Workbench API

    The API for the AllOfUs workbench.

    OpenAPI spec version: 0.1.0
    
    Generated by: https://github.com/swagger-api/swagger-codegen.git
"""


from __future__ import absolute_import

import sys
import os
import re

# python 2 and python 3 compatibility library
from six import iteritems

from ..api_client import ApiClient


class CohortAnnotationDefinitionApi(object):
    """
    NOTE: This class is auto generated by the swagger code generator program.
    Do not edit the class manually.
    Ref: https://github.com/swagger-api/swagger-codegen
    """

    def __init__(self, api_client=None):
        if api_client is None:
            api_client = ApiClient()
        self.api_client = api_client

    def create_cohort_annotation_definition(self, workspace_namespace, workspace_id, cohort_id, request, **kwargs):
        """
        This endpoint will create a CohortAnnotationDefinition.
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async=True
        >>> thread = api.create_cohort_annotation_definition(workspace_namespace, workspace_id, cohort_id, request, async=True)
        >>> result = thread.get()

        :param async bool
        :param str workspace_namespace: (required)
        :param str workspace_id: specifies which workspace (required)
        :param int cohort_id: specifies which cohort (required)
        :param CohortAnnotationDefinition request: CohortAnnotationDefinition creation request body (required)
        :return: CohortAnnotationDefinition
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async'):
            return self.create_cohort_annotation_definition_with_http_info(workspace_namespace, workspace_id, cohort_id, request, **kwargs)
        else:
            (data) = self.create_cohort_annotation_definition_with_http_info(workspace_namespace, workspace_id, cohort_id, request, **kwargs)
            return data

    def create_cohort_annotation_definition_with_http_info(self, workspace_namespace, workspace_id, cohort_id, request, **kwargs):
        """
        This endpoint will create a CohortAnnotationDefinition.
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async=True
        >>> thread = api.create_cohort_annotation_definition_with_http_info(workspace_namespace, workspace_id, cohort_id, request, async=True)
        >>> result = thread.get()

        :param async bool
        :param str workspace_namespace: (required)
        :param str workspace_id: specifies which workspace (required)
        :param int cohort_id: specifies which cohort (required)
        :param CohortAnnotationDefinition request: CohortAnnotationDefinition creation request body (required)
        :return: CohortAnnotationDefinition
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['workspace_namespace', 'workspace_id', 'cohort_id', 'request']
        all_params.append('async')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method create_cohort_annotation_definition" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'workspace_namespace' is set
        if ('workspace_namespace' not in params) or (params['workspace_namespace'] is None):
            raise ValueError("Missing the required parameter `workspace_namespace` when calling `create_cohort_annotation_definition`")
        # verify the required parameter 'workspace_id' is set
        if ('workspace_id' not in params) or (params['workspace_id'] is None):
            raise ValueError("Missing the required parameter `workspace_id` when calling `create_cohort_annotation_definition`")
        # verify the required parameter 'cohort_id' is set
        if ('cohort_id' not in params) or (params['cohort_id'] is None):
            raise ValueError("Missing the required parameter `cohort_id` when calling `create_cohort_annotation_definition`")
        # verify the required parameter 'request' is set
        if ('request' not in params) or (params['request'] is None):
            raise ValueError("Missing the required parameter `request` when calling `create_cohort_annotation_definition`")


        collection_formats = {}

        path_params = {}
        if 'workspace_namespace' in params:
            path_params['workspaceNamespace'] = params['workspace_namespace']
        if 'workspace_id' in params:
            path_params['workspaceId'] = params['workspace_id']
        if 'cohort_id' in params:
            path_params['cohortId'] = params['cohort_id']

        query_params = []

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        if 'request' in params:
            body_params = params['request']
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.\
            select_header_accept(['application/json'])

        # Authentication setting
        auth_settings = ['aou_oauth']

        return self.api_client.call_api('/api/v1/workspaces/{workspaceNamespace}/{workspaceId}/cohorts/{cohortId}/annotationdefinitions', 'POST',
                                        path_params,
                                        query_params,
                                        header_params,
                                        body=body_params,
                                        post_params=form_params,
                                        files=local_var_files,
                                        response_type='CohortAnnotationDefinition',
                                        auth_settings=auth_settings,
                                        async=params.get('async'),
                                        _return_http_data_only=params.get('_return_http_data_only'),
                                        _preload_content=params.get('_preload_content', True),
                                        _request_timeout=params.get('_request_timeout'),
                                        collection_formats=collection_formats)

    def delete_cohort_annotation_definition(self, workspace_namespace, workspace_id, cohort_id, annotation_definition_id, **kwargs):
        """
        Deletes the CohortAnnotationDefinition with the specified ID
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async=True
        >>> thread = api.delete_cohort_annotation_definition(workspace_namespace, workspace_id, cohort_id, annotation_definition_id, async=True)
        >>> result = thread.get()

        :param async bool
        :param str workspace_namespace: specifies which workspace namespace (required)
        :param str workspace_id: specifies which workspace (required)
        :param int cohort_id: specifies which cohort (required)
        :param int annotation_definition_id: specifies which CohortAnnotationDefinition. (required)
        :return: EmptyResponse
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async'):
            return self.delete_cohort_annotation_definition_with_http_info(workspace_namespace, workspace_id, cohort_id, annotation_definition_id, **kwargs)
        else:
            (data) = self.delete_cohort_annotation_definition_with_http_info(workspace_namespace, workspace_id, cohort_id, annotation_definition_id, **kwargs)
            return data

    def delete_cohort_annotation_definition_with_http_info(self, workspace_namespace, workspace_id, cohort_id, annotation_definition_id, **kwargs):
        """
        Deletes the CohortAnnotationDefinition with the specified ID
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async=True
        >>> thread = api.delete_cohort_annotation_definition_with_http_info(workspace_namespace, workspace_id, cohort_id, annotation_definition_id, async=True)
        >>> result = thread.get()

        :param async bool
        :param str workspace_namespace: specifies which workspace namespace (required)
        :param str workspace_id: specifies which workspace (required)
        :param int cohort_id: specifies which cohort (required)
        :param int annotation_definition_id: specifies which CohortAnnotationDefinition. (required)
        :return: EmptyResponse
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['workspace_namespace', 'workspace_id', 'cohort_id', 'annotation_definition_id']
        all_params.append('async')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method delete_cohort_annotation_definition" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'workspace_namespace' is set
        if ('workspace_namespace' not in params) or (params['workspace_namespace'] is None):
            raise ValueError("Missing the required parameter `workspace_namespace` when calling `delete_cohort_annotation_definition`")
        # verify the required parameter 'workspace_id' is set
        if ('workspace_id' not in params) or (params['workspace_id'] is None):
            raise ValueError("Missing the required parameter `workspace_id` when calling `delete_cohort_annotation_definition`")
        # verify the required parameter 'cohort_id' is set
        if ('cohort_id' not in params) or (params['cohort_id'] is None):
            raise ValueError("Missing the required parameter `cohort_id` when calling `delete_cohort_annotation_definition`")
        # verify the required parameter 'annotation_definition_id' is set
        if ('annotation_definition_id' not in params) or (params['annotation_definition_id'] is None):
            raise ValueError("Missing the required parameter `annotation_definition_id` when calling `delete_cohort_annotation_definition`")


        collection_formats = {}

        path_params = {}
        if 'workspace_namespace' in params:
            path_params['workspaceNamespace'] = params['workspace_namespace']
        if 'workspace_id' in params:
            path_params['workspaceId'] = params['workspace_id']
        if 'cohort_id' in params:
            path_params['cohortId'] = params['cohort_id']
        if 'annotation_definition_id' in params:
            path_params['annotationDefinitionId'] = params['annotation_definition_id']

        query_params = []

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.\
            select_header_accept(['application/json'])

        # Authentication setting
        auth_settings = ['aou_oauth']

        return self.api_client.call_api('/api/v1/workspaces/{workspaceNamespace}/{workspaceId}/cohorts/{cohortId}/annotationdefinitions/{annotationDefinitionId}', 'DELETE',
                                        path_params,
                                        query_params,
                                        header_params,
                                        body=body_params,
                                        post_params=form_params,
                                        files=local_var_files,
                                        response_type='EmptyResponse',
                                        auth_settings=auth_settings,
                                        async=params.get('async'),
                                        _return_http_data_only=params.get('_return_http_data_only'),
                                        _preload_content=params.get('_preload_content', True),
                                        _request_timeout=params.get('_request_timeout'),
                                        collection_formats=collection_formats)

    def get_cohort_annotation_definition(self, workspace_namespace, workspace_id, cohort_id, annotation_definition_id, **kwargs):
        """
        Returns a CohortAnnotationDefinition.
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async=True
        >>> thread = api.get_cohort_annotation_definition(workspace_namespace, workspace_id, cohort_id, annotation_definition_id, async=True)
        >>> result = thread.get()

        :param async bool
        :param str workspace_namespace: (required)
        :param str workspace_id: specifies which workspace (required)
        :param int cohort_id: specifies which cohort (required)
        :param int annotation_definition_id: specifies which CohortAnnotationDefinition. (required)
        :return: CohortAnnotationDefinition
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async'):
            return self.get_cohort_annotation_definition_with_http_info(workspace_namespace, workspace_id, cohort_id, annotation_definition_id, **kwargs)
        else:
            (data) = self.get_cohort_annotation_definition_with_http_info(workspace_namespace, workspace_id, cohort_id, annotation_definition_id, **kwargs)
            return data

    def get_cohort_annotation_definition_with_http_info(self, workspace_namespace, workspace_id, cohort_id, annotation_definition_id, **kwargs):
        """
        Returns a CohortAnnotationDefinition.
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async=True
        >>> thread = api.get_cohort_annotation_definition_with_http_info(workspace_namespace, workspace_id, cohort_id, annotation_definition_id, async=True)
        >>> result = thread.get()

        :param async bool
        :param str workspace_namespace: (required)
        :param str workspace_id: specifies which workspace (required)
        :param int cohort_id: specifies which cohort (required)
        :param int annotation_definition_id: specifies which CohortAnnotationDefinition. (required)
        :return: CohortAnnotationDefinition
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['workspace_namespace', 'workspace_id', 'cohort_id', 'annotation_definition_id']
        all_params.append('async')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method get_cohort_annotation_definition" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'workspace_namespace' is set
        if ('workspace_namespace' not in params) or (params['workspace_namespace'] is None):
            raise ValueError("Missing the required parameter `workspace_namespace` when calling `get_cohort_annotation_definition`")
        # verify the required parameter 'workspace_id' is set
        if ('workspace_id' not in params) or (params['workspace_id'] is None):
            raise ValueError("Missing the required parameter `workspace_id` when calling `get_cohort_annotation_definition`")
        # verify the required parameter 'cohort_id' is set
        if ('cohort_id' not in params) or (params['cohort_id'] is None):
            raise ValueError("Missing the required parameter `cohort_id` when calling `get_cohort_annotation_definition`")
        # verify the required parameter 'annotation_definition_id' is set
        if ('annotation_definition_id' not in params) or (params['annotation_definition_id'] is None):
            raise ValueError("Missing the required parameter `annotation_definition_id` when calling `get_cohort_annotation_definition`")


        collection_formats = {}

        path_params = {}
        if 'workspace_namespace' in params:
            path_params['workspaceNamespace'] = params['workspace_namespace']
        if 'workspace_id' in params:
            path_params['workspaceId'] = params['workspace_id']
        if 'cohort_id' in params:
            path_params['cohortId'] = params['cohort_id']
        if 'annotation_definition_id' in params:
            path_params['annotationDefinitionId'] = params['annotation_definition_id']

        query_params = []

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.\
            select_header_accept(['application/json'])

        # Authentication setting
        auth_settings = ['aou_oauth']

        return self.api_client.call_api('/api/v1/workspaces/{workspaceNamespace}/{workspaceId}/cohorts/{cohortId}/annotationdefinitions/{annotationDefinitionId}', 'GET',
                                        path_params,
                                        query_params,
                                        header_params,
                                        body=body_params,
                                        post_params=form_params,
                                        files=local_var_files,
                                        response_type='CohortAnnotationDefinition',
                                        auth_settings=auth_settings,
                                        async=params.get('async'),
                                        _return_http_data_only=params.get('_return_http_data_only'),
                                        _preload_content=params.get('_preload_content', True),
                                        _request_timeout=params.get('_request_timeout'),
                                        collection_formats=collection_formats)

    def get_cohort_annotation_definitions(self, workspace_namespace, workspace_id, cohort_id, **kwargs):
        """
        Returns a collection of CohortAnnotationDefinition.
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async=True
        >>> thread = api.get_cohort_annotation_definitions(workspace_namespace, workspace_id, cohort_id, async=True)
        >>> result = thread.get()

        :param async bool
        :param str workspace_namespace: (required)
        :param str workspace_id: specifies which workspace (required)
        :param int cohort_id: specifies which cohort (required)
        :return: CohortAnnotationDefinitionListResponse
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async'):
            return self.get_cohort_annotation_definitions_with_http_info(workspace_namespace, workspace_id, cohort_id, **kwargs)
        else:
            (data) = self.get_cohort_annotation_definitions_with_http_info(workspace_namespace, workspace_id, cohort_id, **kwargs)
            return data

    def get_cohort_annotation_definitions_with_http_info(self, workspace_namespace, workspace_id, cohort_id, **kwargs):
        """
        Returns a collection of CohortAnnotationDefinition.
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async=True
        >>> thread = api.get_cohort_annotation_definitions_with_http_info(workspace_namespace, workspace_id, cohort_id, async=True)
        >>> result = thread.get()

        :param async bool
        :param str workspace_namespace: (required)
        :param str workspace_id: specifies which workspace (required)
        :param int cohort_id: specifies which cohort (required)
        :return: CohortAnnotationDefinitionListResponse
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['workspace_namespace', 'workspace_id', 'cohort_id']
        all_params.append('async')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method get_cohort_annotation_definitions" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'workspace_namespace' is set
        if ('workspace_namespace' not in params) or (params['workspace_namespace'] is None):
            raise ValueError("Missing the required parameter `workspace_namespace` when calling `get_cohort_annotation_definitions`")
        # verify the required parameter 'workspace_id' is set
        if ('workspace_id' not in params) or (params['workspace_id'] is None):
            raise ValueError("Missing the required parameter `workspace_id` when calling `get_cohort_annotation_definitions`")
        # verify the required parameter 'cohort_id' is set
        if ('cohort_id' not in params) or (params['cohort_id'] is None):
            raise ValueError("Missing the required parameter `cohort_id` when calling `get_cohort_annotation_definitions`")


        collection_formats = {}

        path_params = {}
        if 'workspace_namespace' in params:
            path_params['workspaceNamespace'] = params['workspace_namespace']
        if 'workspace_id' in params:
            path_params['workspaceId'] = params['workspace_id']
        if 'cohort_id' in params:
            path_params['cohortId'] = params['cohort_id']

        query_params = []

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.\
            select_header_accept(['application/json'])

        # Authentication setting
        auth_settings = ['aou_oauth']

        return self.api_client.call_api('/api/v1/workspaces/{workspaceNamespace}/{workspaceId}/cohorts/{cohortId}/annotationdefinitions', 'GET',
                                        path_params,
                                        query_params,
                                        header_params,
                                        body=body_params,
                                        post_params=form_params,
                                        files=local_var_files,
                                        response_type='CohortAnnotationDefinitionListResponse',
                                        auth_settings=auth_settings,
                                        async=params.get('async'),
                                        _return_http_data_only=params.get('_return_http_data_only'),
                                        _preload_content=params.get('_preload_content', True),
                                        _request_timeout=params.get('_request_timeout'),
                                        collection_formats=collection_formats)

    def update_cohort_annotation_definition(self, workspace_namespace, workspace_id, cohort_id, annotation_definition_id, **kwargs):
        """
        modify the CohortAnnotationDefinition.
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async=True
        >>> thread = api.update_cohort_annotation_definition(workspace_namespace, workspace_id, cohort_id, annotation_definition_id, async=True)
        >>> result = thread.get()

        :param async bool
        :param str workspace_namespace: specifies which workspace namespace (required)
        :param str workspace_id: specifies which workspace (required)
        :param int cohort_id: specifies which cohort (required)
        :param int annotation_definition_id: specifies which CohortAnnotationDefinition. (required)
        :param ModifyCohortAnnotationDefinitionRequest modify_cohort_annotation_definition_request: Contains the new CohortAnnotationDefinition
        :return: CohortAnnotationDefinition
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async'):
            return self.update_cohort_annotation_definition_with_http_info(workspace_namespace, workspace_id, cohort_id, annotation_definition_id, **kwargs)
        else:
            (data) = self.update_cohort_annotation_definition_with_http_info(workspace_namespace, workspace_id, cohort_id, annotation_definition_id, **kwargs)
            return data

    def update_cohort_annotation_definition_with_http_info(self, workspace_namespace, workspace_id, cohort_id, annotation_definition_id, **kwargs):
        """
        modify the CohortAnnotationDefinition.
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async=True
        >>> thread = api.update_cohort_annotation_definition_with_http_info(workspace_namespace, workspace_id, cohort_id, annotation_definition_id, async=True)
        >>> result = thread.get()

        :param async bool
        :param str workspace_namespace: specifies which workspace namespace (required)
        :param str workspace_id: specifies which workspace (required)
        :param int cohort_id: specifies which cohort (required)
        :param int annotation_definition_id: specifies which CohortAnnotationDefinition. (required)
        :param ModifyCohortAnnotationDefinitionRequest modify_cohort_annotation_definition_request: Contains the new CohortAnnotationDefinition
        :return: CohortAnnotationDefinition
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['workspace_namespace', 'workspace_id', 'cohort_id', 'annotation_definition_id', 'modify_cohort_annotation_definition_request']
        all_params.append('async')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method update_cohort_annotation_definition" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'workspace_namespace' is set
        if ('workspace_namespace' not in params) or (params['workspace_namespace'] is None):
            raise ValueError("Missing the required parameter `workspace_namespace` when calling `update_cohort_annotation_definition`")
        # verify the required parameter 'workspace_id' is set
        if ('workspace_id' not in params) or (params['workspace_id'] is None):
            raise ValueError("Missing the required parameter `workspace_id` when calling `update_cohort_annotation_definition`")
        # verify the required parameter 'cohort_id' is set
        if ('cohort_id' not in params) or (params['cohort_id'] is None):
            raise ValueError("Missing the required parameter `cohort_id` when calling `update_cohort_annotation_definition`")
        # verify the required parameter 'annotation_definition_id' is set
        if ('annotation_definition_id' not in params) or (params['annotation_definition_id'] is None):
            raise ValueError("Missing the required parameter `annotation_definition_id` when calling `update_cohort_annotation_definition`")


        collection_formats = {}

        path_params = {}
        if 'workspace_namespace' in params:
            path_params['workspaceNamespace'] = params['workspace_namespace']
        if 'workspace_id' in params:
            path_params['workspaceId'] = params['workspace_id']
        if 'cohort_id' in params:
            path_params['cohortId'] = params['cohort_id']
        if 'annotation_definition_id' in params:
            path_params['annotationDefinitionId'] = params['annotation_definition_id']

        query_params = []

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        if 'modify_cohort_annotation_definition_request' in params:
            body_params = params['modify_cohort_annotation_definition_request']
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.\
            select_header_accept(['application/json'])

        # Authentication setting
        auth_settings = ['aou_oauth']

        return self.api_client.call_api('/api/v1/workspaces/{workspaceNamespace}/{workspaceId}/cohorts/{cohortId}/annotationdefinitions/{annotationDefinitionId}', 'PUT',
                                        path_params,
                                        query_params,
                                        header_params,
                                        body=body_params,
                                        post_params=form_params,
                                        files=local_var_files,
                                        response_type='CohortAnnotationDefinition',
                                        auth_settings=auth_settings,
                                        async=params.get('async'),
                                        _return_http_data_only=params.get('_return_http_data_only'),
                                        _preload_content=params.get('_preload_content', True),
                                        _request_timeout=params.get('_request_timeout'),
                                        collection_formats=collection_formats)
